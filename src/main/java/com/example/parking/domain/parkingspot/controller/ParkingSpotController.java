package com.example.parking.domain.parkingspot.controller;

import com.example.parking.domain.parkingspot.dto.ParkingSpotDto;
import com.example.parking.domain.parkingspot.service.ParkingSpotService;
import com.example.parking.global.response.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "주차 자리", description = "주차 자리 관련 API")
public class ParkingSpotController {
  private final ParkingSpotService parkingSpotService;

  // [CUS-02] 예약 가능한 자리 조회 - 특정 주차장에서 현재 예약이 가능한 주차 면 조회
  @Operation(summary = "예약 가능한 자리 조회", description = "특정 주차장에서 현재 예약 가능한 주차 자리를 조회합니다.")
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "조회 완료"),
          @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @GetMapping("/{parkingLotId}/spots/available")
  public ResponseEntity<RsData<List<ParkingSpotDto>>> getAvailableSpots(@PathVariable Long parkingLotId) {
    List<ParkingSpotDto> data = parkingSpotService.findAvailableSpots(parkingLotId);
    return ResponseEntity.ok(new RsData<>("예약 가능한 자리 조회가 완료되었습니다.", "200-1", data));
  }

  // 주차장별 전체 자리 조회 - 특정 주차장의 모든 주차 면 상태 조회
  @Operation(summary = "전체 자리 조회", description = "특정 주차장의 모든 주차 자리 상태를 조회합니다.")
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "조회 완료"),
          @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @GetMapping("/{parkingLotId}/spots")
  public ResponseEntity<RsData<List<ParkingSpotDto>>> getAllSpots(@PathVariable Long parkingLotId) {
    List<ParkingSpotDto> data = parkingSpotService.findAllSpots(parkingLotId);
    return ResponseEntity.ok(new RsData<>("전체 자리 조회가 완료되었습니다.", "200-2", data));
  }

  @Operation(summary = "자리 상태 SSE 구독", description = "특정 주차장의 자리 상태 변경을 실시간으로 수신합니다.")
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "구독 연결 완료")
  })
  @GetMapping(value = "/{parkingLotId}/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter subscribe(@PathVariable Long parkingLotId) {
    return parkingSpotService.subscribe(parkingLotId);
  }


}