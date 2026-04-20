package com.example.parking.domain.parkingspot.controller;

import com.example.parking.domain.parkingspot.dto.ParkingSpotDto;
import com.example.parking.domain.parkingspot.service.ParkingSpotService;
import com.example.parking.global.response.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/parking-spots")
@RequiredArgsConstructor
public class ParkingSpotController {
  private final ParkingSpotService parkingSpotService;

  // [CUS-02] 예약 가능한 자리 조회 - 특정 주차장에서 현재 예약이 가능한 주차 면 조회
  @GetMapping("/{parkingLotId}/spots/available")
  public ResponseEntity<RsData<List<ParkingSpotDto>>> getAvailableSpots(@PathVariable Long parkingLotId) {
    List<ParkingSpotDto> data = parkingSpotService.findAvailableSpots(parkingLotId);
    return ResponseEntity.ok(new RsData<>("예약 가능한 자리 조회가 완료되었습니다.", "200-1", data));
  }

  // 주차장별 전체 자리 조회 - 특정 주차장의 모든 주차 면 상태 조회
  @GetMapping("/{parkingLotId}/spots")
  public ResponseEntity<RsData<List<ParkingSpotDto>>> getAllSpots(@PathVariable Long parkingLotId) {
    List<ParkingSpotDto> data = parkingSpotService.findAllSpots(parkingLotId);
    return ResponseEntity.ok(new RsData<>("전체 자리 조회가 완료되었습니다.", "200-2", data));
  }

  @GetMapping(value = "/{parkingLotId}/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter subscribe(@PathVariable Long parkingLotId) {
    return parkingSpotService.subscribe(parkingLotId);
  }


}