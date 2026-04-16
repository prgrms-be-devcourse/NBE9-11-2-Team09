package com.example.parking.domain.parkingLot.repository;

import com.example.parking.domain.parkingLot.entity.ParkingLot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParkingLotRepository extends JpaRepository<ParkingLot, Long> {
    // 주차장 이름으로 검색
//    List<ParkingLot> findByNameContaining(String name);
    Optional<ParkingLot> findByExternalId(String externalId);

    List<ParkingLot> findByAddressContaining(String dong);
}