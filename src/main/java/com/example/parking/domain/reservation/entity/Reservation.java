package com.example.parking.domain.reservation.entity;

import com.example.parking.domain.parking.entity.ParkingLot;
import com.example.parking.domain.parkingspot.entity.ParkingSpot;
import com.example.parking.domain.user.entity.User;
// 아직 ParkingLot, ParkingSpot 엔티티가 없다면 임포트 에러가 날 수 있으니 패키지 경로를 확인하세요.
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Long id;

    // FK: User 테이블 참조
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // FK: ParkingLot 테이블 참조 (엔티티가 생성된 후 연결하세요)
     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "parking_lot_id", nullable = false)
     private ParkingLot parkingLot;

    // FK: 주차 자리 테이블 참조 (엔티티가 생성된 후 연결하세요)
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

    @Builder
    public Reservation(User user, LocalDateTime startTime, LocalDateTime endTime) {
        this.user = user;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = ReservationStatus.PENDING; // 기본값 PENDING
    }

    // 예약 취소 시 상태 변경 메서드
    public void cancel() {
        this.status = ReservationStatus.CANCELED;
        this.canceledAt = LocalDateTime.now();
    }
}