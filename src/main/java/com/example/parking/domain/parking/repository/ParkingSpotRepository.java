package com.example.parking.domain.parking.repository;

import com.example.parking.domain.parking.entity.ParkingSpot;
import com.example.parking.domain.parking.entity.SpotStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ParkingSpotRepository extends JpaRepository<ParkingSpot, Long> {
//    // 특정 주차장의 모든 자리 조회
//    List<ParkingSpot> findByParkingLotId(Long parkingLotId);
//
//    // 특정 주차장의 상태별(예: AVAILABLE) 자리 조회
//    List<ParkingSpot> findByParkingLotIdAndStatus(Long parkingLotId, SpotStatus status);
}