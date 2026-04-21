package com.example.parking.domain.admin.parkingspot.controller;

import com.example.parking.domain.parkingspot.entity.SpotStatus;
import com.example.parking.domain.parkingspot.service.ParkingSpotService;
import com.example.parking.global.response.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/parking-spots")
public class AdminParkingSpotController {

  private final ParkingSpotService parkingSpotService;

  // 관리자 수동 주차 상태 변경
  @PatchMapping("/{spotId}/status")
  public ResponseEntity<RsData<Void>> updateSpotStatus(
      @PathVariable Long spotId,
      @RequestParam SpotStatus status) {
    parkingSpotService.updateSpotStatusByAdmin(spotId, status);
    return ResponseEntity.ok(new RsData<>("자리 상태가 " + status + "로 변경되었습니다.", "200-1"));
  }
}