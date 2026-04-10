package com.example.parking.domain.parking.entity;

import com.example.parking.domain.parkingspot.entity.ParkingSpot;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "parking_lots")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ParkingLot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "parking_lot_id")
    private Long id;

    @Column(name = "parking_lot_name", nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(nullable = false)
    private Integer totalSpot;

    @Column(nullable = false)
    private Integer price;

    @Column(name = "operation_start_time", nullable = false)
    private LocalTime operationStartTime;

    @Column(name = "operation_end_time", nullable = false)
    private LocalTime operationEndTime;

    // 주차장에 속한 자리 리스트 (양방향 매핑)
    @OneToMany(mappedBy = "parkingLot", cascade = CascadeType.ALL)
    private List<ParkingSpot> spots = new ArrayList<>();

    @Builder
    public ParkingLot(String name, String address, Integer totalSpot, Integer price,
                      LocalTime operationStartTime, LocalTime operationEndTime) {
        this.name = name;
        this.address = address;
        this.totalSpot = totalSpot;
        this.price = price;
        this.operationStartTime = operationStartTime;
        this.operationEndTime = operationEndTime;
    }
}