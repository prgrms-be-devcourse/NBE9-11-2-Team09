package com.example.parking.domain.admin.reservation.service;

import com.example.parking.domain.admin.reservation.dto.AdminReservationResDto;
import com.example.parking.domain.reservation.entity.Reservation;
import com.example.parking.domain.reservation.entity.ReservationStatus;
import com.example.parking.domain.reservation.repository.ReservationRepository;
import com.example.parking.domain.reservation.service.ReservationService; // 주입 추가
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationService reservationService; // 주입 추가

    // [ADM-01] 관리자 - 예약 목록 조회
    public Page<AdminReservationResDto> getAdminReservations(Long userId, Pageable pageable) {
        if (userId != null) {
            return reservationRepository.findAllByUserIdWithDetailsPage(userId, pageable)
                    .map(AdminReservationResDto::from);
        }
        return reservationRepository.findAllWithDetailsPage(pageable)
                .map(AdminReservationResDto::from);
    }

    // [ADM-01] 관리자 - 예약 강제 취소
    @Transactional
    public void cancelReservationByAdmin(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다."));

        LocalDateTime now = LocalDateTime.now();

        // 관리자 제약 조건 검증
        // 1. 결제가 완료된(CONFIRMED) 상태여야 함
        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new IllegalStateException("결제가 완료되지 않은 예약은 관리자가 취소할 수 없습니다.");
        }

        // 2. 실제 입차 시간(startTime) 전이어야 함
        if (now.isAfter(reservation.getStartTime())) {
            throw new IllegalStateException("이미 입차 시간이 지난 예약은 관리자가 취소할 수 없습니다.");
        }

        //중앙화된 취소/환불 로직 호출
        // 관리자이므로 userId는 null을, isForced는 true를 전달합니다.
        reservationService.cancelReservation(reservationId, null, true);

        log.info("[관리자 강제 취소 성공] 예약 ID: {} 가 취소 및 환불 처리되었습니다.", reservationId);
    }


}