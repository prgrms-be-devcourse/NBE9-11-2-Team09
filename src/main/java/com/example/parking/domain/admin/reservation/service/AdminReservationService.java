package com.example.parking.domain.admin.reservation.service;

import com.example.parking.domain.admin.reservation.dto.AdminReservationResDto;
import com.example.parking.domain.parkingspot.entity.SpotStatus;
import com.example.parking.domain.reservation.entity.Reservation;
import com.example.parking.domain.reservation.entity.ReservationStatus;
import com.example.parking.domain.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminReservationService {

    private final ReservationRepository reservationRepository;

    // [ADM-01] 관리자 - 예약 목록 조회 (특정 고객 ID 필터링 포함)
    public Page<AdminReservationResDto> getAdminReservations(Long userId, Pageable pageable) {
        if (userId != null) {
            // 수정됨: findAllByUserId -> findAllByUserIdWithDetailsPage
            return reservationRepository.findAllByUserIdWithDetailsPage(userId, pageable)
                    .map(AdminReservationResDto::from);
        }
        // 수정됨: findAll -> findAllWithDetailsPage
        return reservationRepository.findAllWithDetailsPage(pageable)
                .map(AdminReservationResDto::from);
    }

    // [ADM-01] 관리자 - 예약 강제 취소 (자리 반환 및 환불 연동)
    @Transactional
    public void cancelReservationByAdmin(Long reservationId) {
        Reservation reservation = reservationRepository.findByIdWithParkingSpot(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다."));

        if (reservation.getStatus() == ReservationStatus.CANCELED) {
            throw new IllegalStateException("이미 취소된 예약입니다.");
        }

        ReservationStatus previousStatus = reservation.getStatus();

        // 1. 예약 엔티티 상태 변경 (cancel() 내부에서 canceledAt 기록)
        reservation.cancel();

        // 2. 주차 자리 반환 (현태님의 release() 호출로 SpotStatus와 reservedAt 초기화)
        if (reservation.getParkingSpot().getStatus() == SpotStatus.OCCUPIED) {
            reservation.getParkingSpot().release();
        }

        // 3. 결제 상태에 따른 환불 처리 분기
        if (previousStatus == ReservationStatus.CONFIRMED) {
            log.info("[관리자 강제 취소] 예약 ID: {} - 결제 완료 건이므로 환불 로직을 실행합니다.", reservationId);
            // TODO: 결제 담당자 환불 API 연동
        }
    }
}