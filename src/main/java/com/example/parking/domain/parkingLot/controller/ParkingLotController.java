package com.example.parking.domain.parkingLot.controller;

import com.example.parking.domain.parkingLot.dto.ParkingLotResDto;
import com.example.parking.domain.parkingLot.service.ParkingLotService;
import com.example.parking.global.response.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parking-lots")
@RequiredArgsConstructor
public class ParkingLotController {

    private final ParkingLotService parkingLotService;

    // [CUS-01] 주차장 목록 조회 - 전체 주차장 조회 또는 동 이름을 통한 검색
    @GetMapping
    public ResponseEntity<RsData<List<ParkingLotResDto>>> getParkingLots(
            @RequestParam(required = false) String dong) {
        List<ParkingLotResDto> data = parkingLotService.findAll(dong);
        return ResponseEntity.ok(new RsData<>("주차장 목록 조회가 완료되었습니다.", "200-1", data));
    }

    // [CUS-01] 주차장 상세 조회 - 주차장 ID를 통해 특정 주차장의 상세 정보 조회
    @GetMapping("/{id}")
    public ResponseEntity<RsData<ParkingLotResDto>> getParkingLot(@PathVariable Long id) {
        ParkingLotResDto data = parkingLotService.findById(id);
        return ResponseEntity.ok(new RsData<>("주차장 상세 조회가 완료되었습니다.", "200-2", data));
    }
}