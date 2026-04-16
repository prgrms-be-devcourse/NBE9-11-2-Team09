package com.example.parking.domain.reservation.dto;

import com.example.parking.domain.reservation.entity.Reservation;
import com.example.parking.domain.reservation.entity.ReservationStatus;
import java.time.LocalDateTime;

public record ReservationResDto(
        Long reservationId,
        String parkingLotName,
        String parkingSpotNumber,
        LocalDateTime startTime,
        LocalDateTime endTime,
        ReservationStatus status
) {
    // 엔티티를 DTO로 변환하는 정적 메서드는 그대로 유지 가능합니다.
    public static ReservationResDto from(Reservation reservation) {
        return new ReservationResDto(
                reservation.getId(),
                reservation.getParkingLot().getName(),
                reservation.getParkingSpot().getNumber(),
                reservation.getStartTime(),
                reservation.getEndTime(),
                reservation.getStatus()
        );
    }
}