package com.example.parking.domain.parkingspot.controller;

import com.example.parking.domain.parkingspot.dto.ParkingSpotDto;
import com.example.parking.domain.parkingspot.dto.ParkingSpotResponseDto;
import com.example.parking.domain.parkingspot.entity.ParkingSpot;
import com.example.parking.domain.parkingspot.service.ParkingSpotService;
import com.example.parking.global.response.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/parking-spots")
@RequiredArgsConstructor
public class ParkingSpotController{
  private final ParkingSpotService parkingSpotService;
//  private final Rq rq;

  // 주차장별 예약 가능한 자리 조회
  @GetMapping("/{parkingLotId}/spots/available")
  public List<ParkingSpotDto> getAvailableSpots(
      @PathVariable Long parkingLotId) {

    List<ParkingSpotDto> spots = parkingSpotService.findAvailableSpots(parkingLotId);
    return spots;
  }

  // 주차장별 전체 자리 조회
  @GetMapping("/{parkingLotId}/spots")
  public List<ParkingSpotDto> getAllSpots(
      @PathVariable Long parkingLotId){
    List<ParkingSpotDto> spots = parkingSpotService.findAllSpots(parkingLotId);

    return spots;
  }

  @GetMapping(value = "/{parkingLotId}/subscribe",
      produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter subscribe(@PathVariable Long parkingLotId) {
    return parkingSpotService.subscribe(parkingLotId);
  }



  //자리 점유. 예약 아님
  @PostMapping("/{spotId}/reserve")
  public RsData<ParkingSpotResponseDto> reserve(
      @PathVariable Long spotId
  ){

//    User actor = rq.getActor();

    ParkingSpot parkingSpot = parkingSpotService.reserve(spotId);

    return new RsData<>(
        "%d번 자리 예약이 성공했습니다.".formatted(parkingSpot.getId()),
        "201-1",
        new ParkingSpotResponseDto(
            new ParkingSpotDto(parkingSpot)
        )
    );
  }


}
