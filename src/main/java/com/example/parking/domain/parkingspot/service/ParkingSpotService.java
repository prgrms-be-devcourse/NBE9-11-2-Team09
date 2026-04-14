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
  //[CUS-02] 주차장 자리 점유
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

  // [CUS-11] 주차장별 자리 생성. 각각의 비율에 맞게 SpotType 분배
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

}
