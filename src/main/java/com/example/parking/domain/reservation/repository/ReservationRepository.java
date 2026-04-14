package com.example.parking.domain.reservation.repository;

import com.example.parking.domain.reservation.entity.Reservation;
import com.example.parking.domain.reservation.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // [CUS-04] 예약 관리 - 내 예약 목록 조회 (N+1 문제 방지 Fetch Join)
    @Query("SELECT r FROM Reservation r JOIN FETCH r.parkingLot JOIN FETCH r.parkingSpot " +
            "WHERE r.user.id = :userId " +
            "AND (:status IS NULL OR r.status = :status)")
    // 🔥 상태 조건 동적 처리
    List<Reservation> findAllByUserIdWithDetails(
            @Param("userId") Long userId,
            @Param("status") ReservationStatus status);

    // [CUS-04] 예약 관리 - 특정 예약 상세 조회 (N+1 문제 방지 Fetch Join)
    @Query("SELECT r FROM Reservation r JOIN FETCH r.parkingLot JOIN FETCH r.parkingSpot " +
            "WHERE r.id = :reservationId AND r.user.id = :userId")
    // 🔥 status 조건 제거
    Optional<Reservation> findByIdAndUserIdWithDetails(
            @Param("reservationId") Long reservationId,
            @Param("userId") Long userId);

    // [CUS-03] 예약 생성 - 특정 자리에 시간이 겹치고 취소되지 않은 예약이 있는지 확인
    // 원리: (기존 시작시간 < 내 종료시간) AND (기존 종료시간 > 내 시작시간) 이면 겹친 것임
    @Query("SELECT COUNT(r) FROM Reservation r " +
            "WHERE r.parkingSpot.id = :spotId " +
            "AND r.status != 'CANCELED' " + // 🔥 취소된 예약은 겹침 검사에서 제외!
            "AND r.startTime < :endTime AND r.endTime > :startTime")
    long countOverlappingReservations(
            @Param("spotId") Long spotId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    List<Reservation> findByStatusAndCreatedAtBefore(ReservationStatus status, LocalDateTime time);
}