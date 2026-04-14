package com.example.parking.domain.parkingspot.entity;

import com.example.parking.domain.parkingLot.entity.ParkingLot;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "parking_spots")
@Getter
@NoArgsConstructor
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
        this.status = SpotStatus.AVAILABLE;
    }

    public void reserve() {
        if (this.status != SpotStatus.AVAILABLE) {
            throw new IllegalStateException("이미 점유된 자리입니다.");
        }
        this.status = SpotStatus.OCCUPIED;
    }
}