package com.example.parking.domain.parkingspot.service;

import com.example.parking.domain.parking.entity.SpotStatus;
import com.example.parking.domain.parkingspot.dto.ParkingSpotDto;
import com.example.parking.domain.parkingspot.entity.ParkingSpot;
import com.example.parking.domain.parkingspot.repository.ParkingSpotRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ParkingSpotService {

  private final ParkingSpotRepository parkingSpotRepository;

  public List<ParkingSpotDto> findAvailableSpots(Long parkingLotId) {
    return parkingSpotRepository.findAll()
        .stream()
        .filter(spot -> spot.getParkingLot().getId().equals(parkingLotId))
        .filter(spot -> spot.getStatus() == SpotStatus.AVAILABLE)
        .map(ParkingSpotDto::new)
        .toList();
  }
  public List<ParkingSpotDto> findAllSpots(Long parkingLotId){

    return parkingSpotRepository.findAll()
        .stream()
        .filter(spot -> spot.getParkingLot().getId().equals(parkingLotId))
        .map(ParkingSpotDto::new)
        .toList();

  }

  @Transactional
  public ParkingSpot reserve(Long spotId) {
    ParkingSpot spot = parkingSpotRepository.findByIdWithLock(spotId)
        .orElseThrow(() -> new EntityNotFoundException("자리를 찾을 수 없습니다."));

    spot.reserve();
    return spot;
  }

}
