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

    // [CUS-11] OCCUPIED 인 parkingSpot을 조회
    List<ParkingSpot> findByStatusAndReservedAtBefore(SpotStatus status, LocalDateTime time);

    List<ParkingSpot> findByStatusAndPaymentStartedAtBefore(SpotStatus status, LocalDateTime deadline);


    // [CUS-11] 추가: 스케줄러의 만료 처리 (CAS 방식)
    @Modifying(clearAutomatically = true)
    @Query("UPDATE ParkingSpot p SET p.status = 'AVAILABLE', p.reservedAt = null " +
        "WHERE p.id = :id AND p.status = 'OCCUPIED' AND p.reservedAt < :expiredTime")
    int releaseExpiredSpot(@Param("id") Long id, @Param("expiredTime") LocalDateTime expiredTime);


    // [CUS-05] 결제 시작: OCCUPIED → PAYING
    @Modifying(clearAutomatically = true)
    @Query("UPDATE ParkingSpot p SET p.status = 'PAYING', p.paymentStartedAt = :now " +
        "WHERE p.id = :id AND p.status = 'OCCUPIED'")
    int startPayment(@Param("id") Long id,
                     @Param("now") LocalDateTime now);

    // [CUS-05] 결제 완료: PAYING → AVAILABLE
    @Modifying(clearAutomatically = true)
    @Query("UPDATE ParkingSpot p SET p.status = 'AVAILABLE', p.reservedAt = null, p.paymentStartedAt = null " +
        "WHERE p.id = :id AND p.status = 'PAYING'")
    int completePayment(@Param("id") Long id);

    // [CUS-05] 결제 실패: PAYING → OCCUPIED (결제 실패시 롤백)
    @Modifying(clearAutomatically = true)
    @Query("UPDATE ParkingSpot p SET p.status = 'OCCUPIED', p.paymentStartedAt = null " +
        "WHERE p.id = :id AND p.status = 'PAYING'")
    int failPayment(@Param("id") Long id);

    // [CUS-05] 결제 해제시 상태 변경
    @Modifying(clearAutomatically = true)
    @Query("UPDATE ParkingSpot p SET p.status = 'AVAILABLE', p.reservedAt = null, p.paymentStartedAt = null " +
        "WHERE p.id = :id AND p.status = 'PAYING' AND p.paymentStartedAt < :deadline")
    int releaseExpiredPaymentById(@Param("id") Long id, @Param("deadline") LocalDateTime deadline);


}