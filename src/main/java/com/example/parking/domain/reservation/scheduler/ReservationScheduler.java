package com.example.parking.domain.reservation.scheduler;

import com.example.parking.domain.parkingspot.entity.SpotStatus;
import com.example.parking.domain.reservation.entity.Reservation;
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

    @Scheduled(cron = "* */10 * * * *")
    @Transactional
    public void handleReservationLifecycle() {
        LocalDateTime now = LocalDateTime.now();

        // 1. [자동 입차] 시작 시간이 되었고 결제 완료된 건 -> PARKED
        autoCheckIn(now);

        // 2. [자동 출차] 종료 시간이 지난 건 -> AVAILABLE
        autoCheckOut(now);
    }

    private void autoCheckIn(LocalDateTime now) {
        List<Reservation> toPark = reservationRepository.findToAutoPark(now);
        for (Reservation res : toPark) {
            res.getParkingSpot().updateStatus(SpotStatus.PARKED);
            log.info("[자동 입차] 시작 시간 도달 - 자리 ID: {} -> PARKED", res.getParkingSpot().getId());
        }
    }

    private void autoCheckOut(LocalDateTime now) {
        List<Reservation> toRelease = reservationRepository.findToRelease(now);
        for (Reservation res : toRelease) {
            res.getParkingSpot().updateStatus(SpotStatus.AVAILABLE);
            res.complete();
            log.info("[자동 출차] 종료 시간 도달 - 자리 ID: {} -> AVAILABLE", res.getParkingSpot().getId());
        }
    }
}