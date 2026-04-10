package com.example.parking.domain.parking.repository;

import com.example.parking.domain.parking.entity.ParkingLot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ParkingLotRepository extends JpaRepository<ParkingLot, Long> {
    // 주차장 이름으로 검색
//    List<ParkingLot> findByNameContaining(String name);
}