package com.example.parking.domain.parkingspot.entity;

import com.example.parking.domain.parkingLot.entity.ParkingLot;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
//@Table(name = "parking_spot", indexes = {
//    @Index(name = "idx_parking_lot_status", columnList = "parking_lot_id, parking_spot_status")
//})
@Table(name = "parking_spot")
@Getter
@NoArgsConstructor
public class ParkingSpot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "parking_spot_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @   JoinColumn(name = "parking_lot_id", nullable = false)
    private ParkingLot parkingLot;

    @Enumerated(EnumType.STRING)
    @Column(name = "parking_spot_status", nullable = false)
    private SpotStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "parking_spot_type", nullable = false)
    private SpotType type;

    @Column(name = "parking_spot_number", nullable = false, length = 50)
    private String number;

    @Column(name = "reserved_at")
    private LocalDateTime reservedAt;

//    @Column(name = "payment_started_at")
//    private LocalDateTime paymentStartedAt;

    @Builder
    public ParkingSpot(ParkingLot parkingLot, String number, SpotType type) {
        this.parkingLot = parkingLot;
        this.number = number;
        this.type = type;
        this.status = SpotStatus.AVAILABLE;
    }

    public static ParkingSpot create(ParkingLot parkingLot, String number, SpotType type) {
        ParkingSpot spot = new ParkingSpot();
        spot.parkingLot = parkingLot;
        spot.number = number;
        spot.type = type;
        spot.status = SpotStatus.AVAILABLE; // 강제 고정
        return spot;
    }

    public void reserve() {
        if (this.status != SpotStatus.AVAILABLE) {
            throw new IllegalStateException("이미 점유된 자리입니다.");
        }
        this.status = SpotStatus.OCCUPIED;
        this.reservedAt = LocalDateTime.now();
    }

    public void release() {
        this.status = SpotStatus.AVAILABLE;
        this.reservedAt = null;
    }


    public void updateStatus(SpotStatus status) {
        this.status = status;
    }


}