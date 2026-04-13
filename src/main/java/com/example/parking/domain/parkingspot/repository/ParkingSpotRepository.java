package com.example.parking.domain.parkingspot.repository;

import com.example.parking.domain.parkingspot.entity.ParkingSpot;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ParkingSpotRepository extends JpaRepository<ParkingSpot, Long> {

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT p FROM ParkingSpot p WHERE p.id = :spotId")
  Optional<ParkingSpot> findByIdWithLock(@Param("spotId") Long spotId);

}