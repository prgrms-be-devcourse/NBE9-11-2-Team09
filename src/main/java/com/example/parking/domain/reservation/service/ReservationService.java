package com.example.parking.domain.reservation.service;

import com.example.parking.domain.reservation.dto.ReservationResDto;
import com.example.parking.domain.reservation.entity.Reservation;
import com.example.parking.domain.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;

    // [CUS-04] 예약 관리 - 내 예약 목록 조회
    public List<ReservationResDto> getMyReservations(Long userId) {
        return reservationRepository.findAllByUserIdWithDetails(userId).stream()
                .map(ReservationResDto::from)
                .collect(Collectors.toList());
    }

    // [CUS-04] 예약 관리 - 내 특정 예약 상세 조회
    public ReservationResDto getReservationDetail(Long reservationId, Long userId) {
        Reservation reservation = reservationRepository.findByIdAndUserIdWithDetails(reservationId, userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 권한이 없는 예약입니다."));
        return ReservationResDto.from(reservation);
    }

    // [CUS-04] 예약 관리 - 예약 취소
    @Transactional
    public void cancelReservation(Long reservationId, Long userId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다."));

        // 권한 검증
        if (!reservation.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("해당 예약을 취소할 권한이 없습니다.");
        }

        reservation.cancel();

        // TODO: 원석님(결제) 환불 로직 연동
        // TODO: 현태님(자리) 상태 AVAILABLE 변경 로직 연동
    }
}