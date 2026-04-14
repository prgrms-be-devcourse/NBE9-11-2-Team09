package com.example.parking.domain.parkingspot.repository;

import com.example.parking.domain.parkingspot.entity.ParkingSpot;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ParkingSpotRepository extends JpaRepository<ParkingSpot, Long> {
//    // 특정 주차장의 모든 자리 조회
//    List<ParkingSpot> findByParkingLotId(Long parkingLotId);
//
//    // 특정 주차장의 상태별(예: AVAILABLE) 자리 조회
//    List<ParkingSpot> findByParkingLotIdAndStatus(Long parkingLotId, SpotStatus status);

    // [CUS-03] 동시성 제어를 위한 비관적 락 적용 조회
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM ParkingSpot p WHERE p.id = :spotId")
    Optional<ParkingSpot> findByIdWithLock(@Param("spotId") Long spotId);
}