package com.example.parking.domain.reservation.scheduler;

import com.example.parking.domain.parkingspot.entity.SpotStatus;
import com.example.parking.domain.reservation.entity.Reservation;
import com.example.parking.domain.reservation.entity.ReservationStatus;
import com.example.parking.domain.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationScheduler {

    private final ReservationRepository reservationRepository;

    @Scheduled(cron = "10 * * * * *")
    @Transactional
    public void cancelExpiredHoldings() {
        LocalDateTime limit = LocalDateTime.now().minusSeconds(10);

        List<Reservation> expiredOnes = reservationRepository.findByStatusAndCreatedAtBefore(
                ReservationStatus.PENDING, limit
        );

        for (Reservation res : expiredOnes) {
            res.cancel(); // 예약 상태 CANCELED로 변경

            // 자리가 OCCUPIED(홀딩) 상태인 경우에만 AVAILABLE로 해제
            if (res.getParkingSpot().getStatus() == SpotStatus.OCCUPIED) {
                res.getParkingSpot().updateStatus(SpotStatus.AVAILABLE);
            }

            log.info("[스케줄러] 미결제 취소 및 자리 해제 완료 - 예약 ID: {}", res.getId());
        }
    }
}