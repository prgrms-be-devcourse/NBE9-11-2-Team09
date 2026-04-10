package com.example.parking.domain.parkingspot.entity;

import com.example.parking.domain.parking.entity.ParkingLot;
import com.example.parking.domain.parking.entity.SpotStatus;
import com.example.parking.domain.parking.entity.SpotType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "parking_spots")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ParkingSpot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "parking_spot_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parking_lot_id", nullable = false)
    private ParkingLot parkingLot;

    @Enumerated(EnumType.STRING)
    @Column(name = "parking_spot_status", nullable = false)
    private SpotStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "parking_spot_type", nullable = false)
    private SpotType type;

    @Column(name = "parking_spot_number", nullable = false, length = 50)
    private String number;

    @Builder
    public ParkingSpot(ParkingLot parkingLot, String number, SpotType type) {
        this.parkingLot = parkingLot;
        this.number = number;
        this.type = type;
        this.status = SpotStatus.AVAILABLE; // 초기 상태는 항상 사용 가능
    }
}