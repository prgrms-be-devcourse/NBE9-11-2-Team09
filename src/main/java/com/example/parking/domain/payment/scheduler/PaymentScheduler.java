package com.example.parking.domain.payment.scheduler;

import com.example.parking.domain.parkingspot.dto.ParkingSpotDto;
import com.example.parking.global.sse.SseEmitterManager;
import com.example.parking.domain.payment.entity.Payment;
import com.example.parking.domain.payment.entity.PaymentStatus;
import com.example.parking.domain.payment.repository.PaymentRepository;
import com.example.parking.domain.reservation.entity.Reservation;
import com.example.parking.domain.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentScheduler {

    private final PaymentRepository paymentRepository;
    // 예약 취소를 위해 주입
    private final ReservationService reservationService;
    private final SseEmitterManager sseEmitterManager;

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void cancelExpiredPayments() {
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(5);

        List<Payment> expiredPayments = paymentRepository
                .findByStatusAndCreatedAtBefore(PaymentStatus.PROCESSING, deadline);

        for (Payment payment : expiredPayments) {
            payment.fail(); // 결제 FAILED

            // 2차 타이머 만료: 예약을 CANCELED로 바꾸고 자리를 AVAILABLE로 돌려놓음
            Reservation res = payment.getReservation();
            res.cancel();
            res.getParkingSpot().release(); // PAYING -> AVAILABLE

            sseEmitterManager.notify(
                    res.getParkingSpot().getParkingLot().getId(),
                    new ParkingSpotDto(res.getParkingSpot())
            );

            log.info("[2차 결제 타임아웃] 결제 ID: {}, 예약 ID: {} 취소 완료",
                    payment.getId(), res.getId());
        }
    }
}