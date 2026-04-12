package com.example.parking.domain.parkingLot.dto;

import com.example.parking.domain.parkingLot.entity.ParkingLot;

import java.time.LocalTime;

public record ParkingLotResDto(
        Long id,
        String name,
        String address,
        Integer totalSpot,
        Integer price,
        LocalTime operationStartTime,
        LocalTime operationEndTime
) {
    public static ParkingLotResDto from(ParkingLot parkingLot) {
        return new ParkingLotResDto(
                parkingLot.getId(),
                parkingLot.getName(),
                parkingLot.getAddress(),
                parkingLot.getTotalSpot(),
                parkingLot.getPrice(),
                parkingLot.getOperationStartTime(),
                parkingLot.getOperationEndTime()
        );
    }
}
