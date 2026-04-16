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

    // 매 1분마다 실행 (매 분 0초)
    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void handleReservationLifecycle() {
        LocalDateTime now = LocalDateTime.now();
        log.info("[스케줄러 실행] 주차 예약 수명 주기 관리 시작: {}", now);

        // 1. 1차 선점 타임아웃 처리 (5분간 결제 진입 안 한 건)
        cleanupSelectionTimeout(now);

        // 2. 자동 입차 처리 (시작 시간 도달)
        autoCheckIn(now);

        // 3. 자동 출차 처리 (종료 시간 도달)
        autoCheckOut(now);
    }

    private void cleanupSelectionTimeout(LocalDateTime now) {
        LocalDateTime limit = now.minusMinutes(5);
        List<Reservation> expired = reservationRepository.findSelectionTimeout(limit);
        for (Reservation res : expired) {
            res.cancel();
            res.getParkingSpot().updateStatus(SpotStatus.AVAILABLE);
            log.info("[선점 만료] 예약 ID: {} 취소 및 자리 반환", res.getId());
        }
    }

    private void autoCheckIn(LocalDateTime now) {
        // 결제 완료(CONFIRMED)된 건 중 시작 시간이 된 건들
        List<Reservation> toIn = reservationRepository.findToAutoCheckIn(now);
        for (Reservation res : toIn) {
            // 물리적 자리 점유: AVAILABLE -> OCCUPIED
            res.getParkingSpot().updateStatus(SpotStatus.OCCUPIED);
            // 예약 상태 변경: CONFIRMED -> COMPLETED
            res.complete();
            log.info("[자동 입차] 예약 ID: {} 시작 시간 도달 - 자리 ID: {} -> OCCUPIED", res.getId(), res.getParkingSpot().getId());
        }
    }

    private void autoCheckOut(LocalDateTime now) {
        // 이용 중(COMPLETED)인 건 중 종료 시간이 지난 건들
        List<Reservation> toOut = reservationRepository.findToAutoCheckOut(now);
        for (Reservation res : toOut) {
            // 물리적 자리 반환: OCCUPIED -> AVAILABLE
            res.getParkingSpot().updateStatus(SpotStatus.AVAILABLE);
            // 예약 상태는 이미 COMPLETED이므로 추가 변경 불필요 (필요 시 로깅만)
            log.info("[자동 출차] 예약 ID: {} 종료 시간 도달 - 자리 ID: {} -> AVAILABLE", res.getId(), res.getParkingSpot().getId());
        }
    }
}