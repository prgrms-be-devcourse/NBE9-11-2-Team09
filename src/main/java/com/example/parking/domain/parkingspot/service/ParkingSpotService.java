package com.example.parking.domain.parkingspot.service;

import com.example.parking.domain.parkingLot.entity.ParkingLot;
import com.example.parking.domain.parkingspot.entity.SpotStatus;
import com.example.parking.domain.parkingspot.entity.SpotType;
import com.example.parking.domain.parkingspot.dto.ParkingSpotDto;
import com.example.parking.domain.parkingspot.entity.ParkingSpot;
import com.example.parking.domain.parkingspot.repository.ParkingSpotRepository;
import com.example.parking.global.sse.SseEmitterManager;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ParkingSpotService {

  private final ParkingSpotRepository parkingSpotRepository;
  private final SseEmitterManager sseEmitterManager;

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

    sseEmitterManager.notify(
        spot.getParkingLot().getId(),
        new ParkingSpotDto(spot)
    );

    return spot;
  }

  public SseEmitter subscribe(Long parkingLotId) {
    return sseEmitterManager.subscribe(parkingLotId);
  }

  public void createSpots(ParkingLot saved, Integer totalSpot) {
  }
}
