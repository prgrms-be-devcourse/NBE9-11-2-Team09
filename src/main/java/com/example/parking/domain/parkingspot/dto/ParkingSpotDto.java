package com.example.parking.domain.parkingspot.dto;

import com.example.parking.domain.parkingspot.entity.ParkingSpot;
import com.example.parking.domain.parkingspot.entity.SpotStatus;
import com.example.parking.domain.parkingspot.entity.SpotType;

public record ParkingSpotDto(
    Long id,
    SpotStatus status,
    SpotType type,
    String number
) {
  public ParkingSpotDto(ParkingSpot parkingSpot){
    this(
        parkingSpot.getId(),
        parkingSpot.getStatus(),
        parkingSpot.getType(),
        parkingSpot.getNumber()
    );
  }
}
