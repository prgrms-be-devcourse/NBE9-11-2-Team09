package com.example.parking.domain.parkingspot.scheduler;

import com.example.parking.domain.parkingspot.dto.ParkingSpotDto;
import com.example.parking.domain.parkingspot.entity.ParkingSpot;
import com.example.parking.domain.parkingspot.entity.SpotStatus;
import com.example.parking.domain.parkingspot.repository.ParkingSpotRepository;
import com.example.parking.domain.payment.entity.Payment;
import com.example.parking.domain.payment.entity.PaymentStatus;
import com.example.parking.domain.payment.repository.PaymentRepository;
import com.example.parking.global.sse.SseEmitterManager;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import com.example.parking.domain.payment.repository.PaymentRepository;
import com.example.parking.domain.payment.entity.Payment;
import com.example.parking.domain.payment.entity.PaymentStatus;

@Component
@RequiredArgsConstructor
public class ParkingSpotScheduler {

  private final ParkingSpotRepository parkingSpotRepository;
  private final SseEmitterManager sseEmitterManager;
  private final PaymentRepository paymentRepository;

  @Scheduled(fixedDelay = 60000) // 1분마다 실행
  @Transactional
  public void releaseExpiredSpots() {
    LocalDateTime deadline = LocalDateTime.now().minusMinutes(5);
    LocalDateTime paymentDeadline = LocalDateTime.now().minusMinutes(3); // 결제 시작 후 3분 경과

    List<ParkingSpot> expiredPayments = parkingSpotRepository
        .findByStatusAndPaymentStartedAtBefore(SpotStatus.PAYING,paymentDeadline);

    // [CUS-11] 만료가 될수있는 후보들을 조회. OCCUPIED 인 parkingSpot을 조회함
    List<ParkingSpot> expiredSpots = parkingSpotRepository
        .findByStatusAndReservedAtBefore(SpotStatus.OCCUPIED, deadline);

    for (ParkingSpot spot : expiredSpots) {
      // [CUS-11] 원자적 업데이트 시도 (CAS 방식)
      // DB에서 여전히 OCCUPIED이고 시간이 만료됐을 때만 AVAILABLE로 바꿉니다.
      int updatedCount = parkingSpotRepository.releaseExpiredSpot(spot.getId(), deadline);

      // [CUS-11] 업데이트에 성공했을 때만 알림 발송
      // updatedCount가 0이라면, 그 찰나에 사용자가 결제해서 AVAILABLE로 바뀐 것입니다.
      if (updatedCount > 0) {
        sseEmitterManager.notify(
            spot.getParkingLot().getId(),
            new ParkingSpotDto(spot)
        );
      }
    }

    for (ParkingSpot spot : expiredPayments) {
      // [CUS-05] 개별 건에 대해 CAS 업데이트 시도
      int updatedCount = parkingSpotRepository.releaseExpiredPaymentById(spot.getId(), paymentDeadline);

      // [CUS-05] 성공했다면 SSE 알림 전송
      if (updatedCount > 0) {
        paymentRepository.findByReservationParkingSpotIdAndStatus(
                        spot.getId(), PaymentStatus.PROCESSING)
                .ifPresent(Payment::fail);
        sseEmitterManager.notify(spot.getParkingLot().getId(), new ParkingSpotDto(spot));
      }
    }
  }
}