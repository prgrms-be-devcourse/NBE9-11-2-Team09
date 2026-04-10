package com.example.parking.domain.parkingspot.repository;

import com.example.parking.domain.parkingspot.entity.ParkingSpot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParkingSpotRepository extends JpaRepository<ParkingSpot, Long> {
//    // 특정 주차장의 모든 자리 조회
//    List<ParkingSpot> findByParkingLotId(Long parkingLotId);
//
//    // 특정 주차장의 상태별(예: AVAILABLE) 자리 조회
//    List<ParkingSpot> findByParkingLotIdAndStatus(Long parkingLotId, SpotStatus status);
}