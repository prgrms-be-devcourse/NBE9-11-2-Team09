package com.example.parking.domain.reservation.entity;

import com.example.parking.domain.parkingLot.entity.ParkingLot;
import com.example.parking.domain.parkingspot.entity.ParkingSpot;
import com.example.parking.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Getter
@Builder
@AllArgsConstructor // 👈 빌더가 내부적으로 사용하는 '모든 필드 생성자' 생성
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 👈 JPA가 사용하는 '기본 생성자' 생성
@EntityListeners(AuditingEntityListener.class)
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parking_lot_id", nullable = false)
    private ParkingLot parkingLot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parking_spot_id", nullable = false)
    private ParkingSpot parkingSpot;

    @Column(name = "parking_start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "parking_end_time", nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReservationStatus status;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    // Builder에 DB 필수값(nullable = false)인 주차장과 자리 파라미터 추가 완료
    @Builder
    public Reservation(User user, ParkingLot parkingLot, ParkingSpot parkingSpot, LocalDateTime startTime, LocalDateTime endTime) {
        this.user = user;
        this.parkingLot = parkingLot;
        this.parkingSpot = parkingSpot;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = ReservationStatus.PENDING;
    }

    public void cancel() {
        this.status = ReservationStatus.CANCELED;
        this.canceledAt = LocalDateTime.now();
    }

    public void confirm() {
        this.status = ReservationStatus.CONFIRMED;
    }


}