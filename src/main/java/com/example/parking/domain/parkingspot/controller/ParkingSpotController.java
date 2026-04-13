package com.example.parking.domain.parkingspot.controller;

import com.example.parking.domain.parkingspot.dto.ParkingSpotDto;
import com.example.parking.domain.parkingspot.entity.ParkingSpot;
import com.example.parking.domain.parkingspot.service.ParkingSpotService;
import com.example.parking.domain.user.entity.User;
import com.example.parking.global.response.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parking-spots")
@RequiredArgsConstructor
public class ParkingSpotController{
  private final ParkingSpotService parkingSpotService;
  private final Rq rq;


  @GetMapping("/{parkingLotId}/spots/available")
  public List<ParkingSpotDto> getAvailableSpots(
      @PathVariable Long parkingLotId) {

    List<ParkingSpotDto> spots = parkingSpotService.findAvailableSpots(parkingLotId);
    return spots;
  }

  @GetMapping("/{parkingLotId}/spots")
  public List<ParkingSpotDto> getAllSpots(
      @PathVariable Long parkingLotId){
    List<ParkingSpotDto> spots = parkingSpotService.findAllSpots(parkingLotId);

    return spots;
  }

  record ParkingSpotResponseDto(
      ParkingSpotDto parkingSpotDto
  ) {
  }

  @PostMapping("/{spotId}/reserve")
  public RsData<ParkingSpotResponseDto> reserve(
      @PathVariable Long spotId
  ){

    User actor = rq.getActor();

    ParkingSpot parkingSpot = parkingSpotService.reserve(actor, spotId);

    return new RsData<>(
        "%d번 게시물이 생성되었습니다.".formatted(parkingSpot.getId()),
        "201-1",
        new ParkingSpotResponseDto(
            new ParkingSpotDto(parkingSpot)
        )
    );
  }


}
