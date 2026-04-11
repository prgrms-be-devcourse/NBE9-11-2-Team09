package com.example.parking.domain.parkingLot.repository;

import com.example.parking.domain.parkingLot.entity.ParkingLot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParkingLotRepository extends JpaRepository<ParkingLot, Long> {
    // 주차장 이름으로 검색
//    List<ParkingLot> findByNameContaining(String name);
}