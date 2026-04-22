package com.example.parking.domain.parkingspot.service;

import com.example.parking.domain.parkingLot.entity.ParkingLot;
import com.example.parking.domain.parkingspot.dto.ParkingSpotDto;
import com.example.parking.domain.parkingspot.entity.ParkingSpot;
import com.example.parking.domain.parkingspot.entity.SpotStatus;
import com.example.parking.domain.parkingspot.entity.SpotType;
import com.example.parking.domain.parkingspot.repository.ParkingSpotRepository;
import com.example.parking.global.sse.SseEmitterManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParkingSpotService {

  private final ParkingSpotRepository parkingSpotRepository;
  private final SseEmitterManager sseEmitterManager;
  //[CUS-11] 이용가능한 자리 목록 반환
  public List<ParkingSpotDto> findAvailableSpots(Long parkingLotId) {
    return parkingSpotRepository
        .findByParkingLotIdAndStatus(parkingLotId, SpotStatus.AVAILABLE)
        .stream()
        .map(ParkingSpotDto::new)
        .toList();

  }
  //[CUS-11] 모든 자리 반환
  public List<ParkingSpotDto> findAllSpots(Long parkingLotId){

    return parkingSpotRepository.findAll()
        .stream()
        .filter(spot -> spot.getParkingLot().getId().equals(parkingLotId))
        .map(ParkingSpotDto::new)
        .toList();

  }
  // [CUS-11]주차자리에 맞는 자리 생성
  @Transactional
  public void createSpots(ParkingLot parkingLot, int totalSpot) {
    List<ParkingSpot> spots = new ArrayList<>();
    int smallCount    = (int) (totalSpot * 0.8);  // 80%
    int largeCount    = (int) (totalSpot * 0.1);  // 10%


    Stream<ParkingSpot> smallStream = IntStream.rangeClosed(1, smallCount).boxed()
        .map(it -> ParkingSpot.create(parkingLot, String.valueOf(it), SpotType.SMALL));

    Stream<ParkingSpot> largeStream = IntStream.rangeClosed(smallCount + 1, smallCount + largeCount).boxed()
        .map(it -> ParkingSpot.create(parkingLot, String.valueOf(it), SpotType.LARGE));

    Stream<ParkingSpot> electricStream = IntStream.rangeClosed(smallCount + largeCount + 1, totalSpot).boxed()
        .map(it -> ParkingSpot.create(parkingLot, String.valueOf(it), SpotType.ELECTRIC));

    smallStream.forEach(spots::add);
    largeStream.forEach(spots::add);
    electricStream.forEach(spots::add);

    parkingSpotRepository.saveAll(spots);
  }
  //[CUS-11] 구독. 주차장내 모든 인원에게 전파
  public SseEmitter subscribe(Long parkingLotId) {
    return sseEmitterManager.subscribe(parkingLotId);
  }
  //[ADM-05] 관리자의 주차자리 상태변경
  @Transactional
  public void updateSpotStatusByAdmin(Long spotId, SpotStatus status) {
    ParkingSpot spot = parkingSpotRepository.findById(spotId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주차 자리입니다."));

    spot.updateStatus(status);

    sseEmitterManager.notify(
        spot.getParkingLot().getId(),
        new ParkingSpotDto(spot)
    );
  }
}
