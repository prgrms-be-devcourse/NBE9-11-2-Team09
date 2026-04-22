package com.example.parking.domain.reservation.repository;

import com.example.parking.domain.reservation.entity.Reservation;
import com.example.parking.domain.reservation.entity.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    //상태 조건 동적 처리
    List<Reservation> findAllByUserIdWithDetails(
            @Param("userId") Long userId,
            @Param("status") ReservationStatus status);

    // [CUS-04] 예약 관리 - 특정 예약 상세 조회 (N+1 문제 방지 Fetch Join)
    @Query("SELECT r FROM Reservation r JOIN FETCH r.parkingLot JOIN FETCH r.parkingSpot " +
            "WHERE r.id = :reservationId AND r.user.id = :userId")
    //status 조건 제거
    Optional<Reservation> findByIdAndUserIdWithDetails(
            @Param("reservationId") Long reservationId,
            @Param("userId") Long userId);

    // [CUS-03] 예약 생성 - 특정 자리에 시간이 겹치고 취소되지 않은 예약이 있는지 확인
    // 원리: (기존 시작시간 < 내 종료시간) AND (기존 종료시간 > 내 시작시간) 이면 겹친 것임
    @Query("SELECT COUNT(r) FROM Reservation r " +
            "WHERE r.parkingSpot.id = :spotId " +
            "AND r.status != 'CANCELED' " + //취소된 예약은 겹침 검사에서 제외
            "AND r.startTime < :endTime AND r.endTime > :startTime")
    long countOverlappingReservations(
            @Param("spotId") Long spotId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );


    @Query("SELECT r FROM Reservation r JOIN FETCH r.parkingSpot WHERE r.id = :id")
    Optional<Reservation> findByIdWithParkingSpot(@Param("id") Long id);

    // [ADM-01] 관리자용 특정 유저 예약 페이징 조회 (N+1 방지)
    @Query(value = "SELECT r FROM Reservation r JOIN FETCH r.user JOIN FETCH r.parkingLot JOIN FETCH r.parkingSpot WHERE r.user.id = :userId",
            countQuery = "SELECT count(r) FROM Reservation r WHERE r.user.id = :userId")
    Page<Reservation> findAllByUserIdWithDetailsPage(@Param("userId") Long userId, Pageable pageable);

    // [ADM-01] 관리자용 전체 예약 페이징 조회 (N+1 방지)
    @Query(value = "SELECT r FROM Reservation r JOIN FETCH r.user JOIN FETCH r.parkingLot JOIN FETCH r.parkingSpot",
            countQuery = "SELECT count(r) FROM Reservation r")
    Page<Reservation> findAllWithDetailsPage(Pageable pageable);

    // 1. [자동 입차 대상] 결제 완료(CONFIRMED) 상태이고 시작 시간이 현재 시간보다 이전인 예약
    @Query("SELECT r FROM Reservation r JOIN FETCH r.parkingSpot " +
            "WHERE r.status = 'CONFIRMED' AND r.startTime <= :now")
    List<Reservation> findToAutoCheckIn(@Param("now") LocalDateTime now);

    // 2. [자동 출차 대상] 이미 입차(COMPLETED) 상태이고 종료 시간이 현재 시간보다 이전인 예약
// 주차 자리가 여전히 PARKED인 경우만 골라냅니다.
    @Query("SELECT r FROM Reservation r JOIN FETCH r.parkingSpot " +
            "WHERE r.status = 'COMPLETED' AND r.endTime <= :now " +
            "AND r.parkingSpot.status = 'PARKED'")
    List<Reservation> findToAutoCheckOut(@Param("now") LocalDateTime now);

    // 3. [1차 선점 타임아웃] PENDING 상태이며 결제 요청 기록 없이 5분이 지난 예약 (기존 파일에 있는 내용 확인)
    @Query("SELECT r FROM Reservation r JOIN FETCH r.parkingSpot " +
            "WHERE r.status = 'PENDING' AND r.paymentRequestedAt IS NULL AND r.createdAt < :limit")
    List<Reservation> findSelectionTimeout(@Param("limit") LocalDateTime limit);

    // 중복 예약 방지 쿼리 (PENDING, CONFIRMED 모두 체크)
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.parkingSpot.id = :spotId " +
            "AND r.status IN ('PENDING', 'CONFIRMED') " +
            "AND (:start < r.endTime AND :end > r.startTime)")
    long countOverlapping(@Param("spotId") Long spotId,
                          @Param("start") LocalDateTime start,
                          @Param("end") LocalDateTime end);

    boolean existsByUserIdAndParkingLotIdAndStatusIn(
        Long userId,
        Long parkingLotId,
        List<ReservationStatus> statuses
    );
}