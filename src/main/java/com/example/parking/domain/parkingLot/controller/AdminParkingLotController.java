package com.example.parking.domain.parkingLot.controller;

import com.example.parking.domain.parkingLot.service.ParkingLotSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/*
[CUS-01] 관리자용 주차장 데이터 동기화 API
 */
//@RestController
//@RequestMapping("/api/admin/parking-lots")
//@RequiredArgsConstructor
//public class AdminParkingLotController {
//
//    private final ParkingLotSyncService parkingLotSyncService;
//
//    @PostMapping("/sync")
//    public void sync() {
//        parkingLotSyncService.syncParkingLots();
//    }
//}
