package com.example.parking.domain.reservation.dto;

import com.example.parking.domain.reservation.entity.Reservation;
import com.example.parking.domain.reservation.entity.ReservationStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReservationResDto {
    private Long reservationId;
    private String parkingLotName;
    private String parkingSpotNumber;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private ReservationStatus status;

    // [CUS-04] 예약 관리 - 엔티티를 DTO로 변환
    public static ReservationResDto from(Reservation reservation) {
        return ReservationResDto.builder()
                .reservationId(reservation.getId())
                .parkingLotName(reservation.getParkingLot().getName())     // ParkingLot의 name 매핑
                .parkingSpotNumber(reservation.getParkingSpot().getNumber()) // ParkingSpot의 number 매핑
                .startTime(reservation.getStartTime())
                .endTime(reservation.getEndTime())
                .status(reservation.getStatus())
                .build();
    }
}