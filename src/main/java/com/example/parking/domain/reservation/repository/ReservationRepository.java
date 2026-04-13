package com.example.parking.domain.reservation.repository;

import com.example.parking.domain.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // [CUS-04] 예약 관리 - 내 예약 목록 조회 (N+1 문제 방지 Fetch Join)
    @Query("SELECT r FROM Reservation r JOIN FETCH r.parkingLot JOIN FETCH r.parkingSpot WHERE r.user.id = :userId")
    List<Reservation> findAllByUserIdWithDetails(@Param("userId") Long userId);

    // [CUS-04] 예약 관리 - 특정 예약 상세 조회 (N+1 문제 방지 Fetch Join)
    @Query("SELECT r FROM Reservation r JOIN FETCH r.parkingLot JOIN FETCH r.parkingSpot WHERE r.id = :reservationId AND r.user.id = :userId")
    Optional<Reservation> findByIdAndUserIdWithDetails(@Param("reservationId") Long reservationId, @Param("userId") Long userId);
}