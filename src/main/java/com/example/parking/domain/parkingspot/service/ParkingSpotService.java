package com.example.parking.domain.parkingspot.service;

import com.example.parking.domain.parkingLot.entity.ParkingLot;
import com.example.parking.domain.parkingspot.dto.ParkingSpotDto;
import com.example.parking.domain.parkingspot.entity.ParkingSpot;
import com.example.parking.domain.parkingspot.entity.SpotStatus;
import com.example.parking.domain.parkingspot.entity.SpotType;
import com.example.parking.domain.parkingspot.repository.ParkingSpotRepository;
import com.example.parking.global.sse.SseEmitterManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
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
  public void createSpots(ParkingLot parkingLot, int totalSpot) {
    List<ParkingSpot> spots = new ArrayList<>();
    int smallCount    = (int) (totalSpot * 0.8);  // 80%
    int largeCount    = (int) (totalSpot * 0.1);  // 10%


    for (int i = 1; i <= smallCount; i++) {
      spots.add(ParkingSpot.create(parkingLot, String.valueOf(i), SpotType.SMALL));
    }
    for (int i = smallCount + 1; i <= smallCount + largeCount; i++) {
      spots.add(ParkingSpot.create(parkingLot, String.valueOf(i), SpotType.LARGE));
    }
    for (int i = smallCount + largeCount + 1; i <= totalSpot; i++) {
      spots.add(ParkingSpot.create(parkingLot, String.valueOf(i), SpotType.ELECTRIC));
    }
    parkingSpotRepository.saveAll(spots);
  }

  @Transactional
  public ParkingSpot reserve(ParkingSpot spot) {

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

}
