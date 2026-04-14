package com.example.parking.domain.parkingspot.repository;

import com.example.parking.domain.parkingspot.entity.ParkingSpot;
import com.example.parking.domain.parkingspot.entity.SpotStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ParkingSpotRepository extends JpaRepository<ParkingSpot, Long> {

    // [CUS-02] 동시성 제어를 위한 비관적 락 적용 조회
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM ParkingSpot p WHERE p.id = :spotId")
    Optional<ParkingSpot> findByIdWithLock(@Param("spotId") Long spotId);

    // [CUS-11] 동시성 제어
    List<ParkingSpot> findByStatusAndReservedAtBefore(SpotStatus status, LocalDateTime time);

//    // [3] 추가: 결제 완료 처리 (CAS 방식)
//    @Modifying(clearAutomatically = true)
//    @Query("UPDATE ParkingSpot p SET p.status = 'RESERVED' " +
//        "WHERE p.id = :id AND p.status = 'OCCUPIED'")
//    int completePayment(@Param("id") Long id);

    // [4] 추가: 스케줄러의 만료 처리 (CAS 방식)
    @Modifying(clearAutomatically = true)
    @Query("UPDATE ParkingSpot p SET p.status = 'AVAILABLE', p.reservedAt = null " +
        "WHERE p.id = :id AND p.status = 'OCCUPIED'")
    int releaseExpiredSpot(@Param("id") Long id);
}