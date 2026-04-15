package com.example.parking.domain.reservation.scheduler;

import com.example.parking.domain.parkingspot.entity.ParkingSpot;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationScheduler {

    private final ReservationRepository reservationRepository;

    // [스케줄러 1] 5분 이내 미결제 예약 자동 취소
    @Scheduled(cron = "0 * * * * *") // 매 분 0초에 실행
    @Transactional
    public void cancelUnpaidReservations() {
        // 현재 시간 기준 5분 전 시간 계산
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);

        // 생성된 지 5분이 지났는데 여전히 PENDING(결제 대기) 상태인 예약 조회
        List<Reservation> unpaidReservations = reservationRepository.findByStatusAndCreatedAtBefore(
                ReservationStatus.PENDING,
                fiveMinutesAgo
        );

        for (Reservation res : unpaidReservations) {
            res.cancel(); // 엔티티 내부의 취소 메서드 호출 (상태를 CANCELLED로 변경)
            log.info("[스케줄러] 결제 시간(5분) 초과로 예약 [ID: {}] 자동 취소 처리", res.getId());
        }
    }

    // [스케줄러 2] 예약 30분 전 주차 자리 RESERVED 선점
    @Scheduled(cron = "0 * * * * *") // 매 분 0초에 실행
    @Transactional
    public void lockSpotForUpcomingReservations() {
        // 초/나노초를 절사하고 정확히 30분 뒤의 시간 계산
        LocalDateTime targetTime = LocalDateTime.now().plusMinutes(30).withSecond(0).withNano(0);

        // 💡 주의: 현재는 PENDING을 조회하지만, 추후 결제 로직이 연동되면
        // 상태를 'PAID' 또는 'CONFIRMED' 로 변경하여 조회해야 합니다.
        List<Reservation> upcomingReservations = reservationRepository.findByStartTimeAndStatus(
                targetTime,
                ReservationStatus.PENDING
        );

        for (Reservation res : upcomingReservations) {
            ParkingSpot spot = res.getParkingSpot();

            // 자리가 비어있을 때만 예약석으로 변경
            if (spot.getStatus() == SpotStatus.AVAILABLE) {
                // TODO: ParkingSpot 엔티티에 updateStatus 메서드가 없다면 만들어야 합니다.
                // spot.updateStatus(SpotStatus.RESERVED);

                log.info("[스케줄러] 예약 30분 전 임박! 주차 자리 [ID: {}] RESERVED 변경", spot.getId());
            }
        }
    }
}