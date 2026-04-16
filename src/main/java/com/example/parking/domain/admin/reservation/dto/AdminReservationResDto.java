package com.example.parking.domain.admin.reservation.dto;

import com.example.parking.domain.reservation.entity.Reservation;
import com.example.parking.domain.reservation.entity.ReservationStatus;

import java.time.LocalDateTime;

/**
 * [ADM-01] 관리자용 예약 정보 응답 DTO
 * record를 사용하여 불변 객체로 구현
 */
public record AdminReservationResDto(
        Long reservationId,
        Long userId,
        String userName,
        String userEmail,
        Long parkingSpotId,
        String parkingLotName,
        LocalDateTime startTime,
        LocalDateTime endTime,
        ReservationStatus status,
        LocalDateTime createdTime
) {
    // 정적 팩토리 메서드는 record 내부에서도 동일하게 사용 가능합니다.
    public static AdminReservationResDto from(Reservation reservation) {
        return new AdminReservationResDto(
                reservation.getId(),
                reservation.getUser().getId(),
                reservation.getUser().getName(),
                reservation.getUser().getEmail(),
                reservation.getParkingSpot().getId(),
                reservation.getParkingLot().getName(),
                reservation.getStartTime(),
                reservation.getEndTime(),
                reservation.getStatus(),
                reservation.getCreatedAt()
        );
    }
}