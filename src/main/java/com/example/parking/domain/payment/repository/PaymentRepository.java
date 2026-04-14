package com.example.parking.domain.payment.repository;

import com.example.parking.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // 중복 결제 방지
    boolean existsByReservationId(Long reservationId);

    // 전체 결제 조회 (N+1 방지)
    @Query("SELECT p FROM Payment p " +
            "JOIN FETCH p.reservation r " +
            "JOIN FETCH r.user u")
    List<Payment> findAllWithReservationAndUser();

    // 고객별 결제 조회 (N+1 방지)
    @Query("SELECT p FROM Payment p " +
            "JOIN FETCH p.reservation r " +
            "JOIN FETCH r.user u " +
            "WHERE r.user.id = :userId")
    List<Payment> findAllByUserIdWithReservationAndUser(@Param("userId") Long userId);
}