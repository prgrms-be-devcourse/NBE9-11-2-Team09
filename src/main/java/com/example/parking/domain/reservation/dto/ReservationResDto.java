package com.example.parking.domain.reservation.dto;

import com.example.parking.domain.reservation.entity.Reservation;
import com.example.parking.domain.reservation.entity.ReservationStatus;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public record ReservationResDto(
        Long reservationId,
        Long parkingLotId,        // 추가
        Long parkingSpotId,       // 추가
        String parkingLotName,
        String parkingSpotNumber,
        LocalDateTime startTime,
        LocalDateTime endTime,
        ReservationStatus status,
        Integer totalPrice        // 추가
) {
    public static ReservationResDto from(Reservation reservation) {
        long minutes = ChronoUnit.MINUTES.between(
                reservation.getStartTime(), reservation.getEndTime()
        );
        int price = (int) Math.ceil(minutes / 10.0) * reservation.getParkingLot().getPrice();

        return new ReservationResDto(
                reservation.getId(),
                reservation.getParkingLot().getId(),       // 추가
                reservation.getParkingSpot().getId(),      // 추가
                reservation.getParkingLot().getName(),
                reservation.getParkingSpot().getNumber(),
                reservation.getStartTime(),
                reservation.getEndTime(),
                reservation.getStatus(),
                price                                      // 추가
        );
    }
}