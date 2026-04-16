package com.example.parking.domain.parkingLot.entity;

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

    // 기본값 세팅
    private static final Integer DEFAULT_PRICE = 1000;
    private static final LocalTime DEFAULT_OPERATION_START_TIME = LocalTime.MIN; // 00:00
    private static final LocalTime DEFAULT_OPERATION_END_TIME = LocalTime.of(23, 59);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "parking_lot_id")
    private Long id;

    // 외부 공공데이터의 주차장 식별값
    @Column(nullable = false, unique = true)
    private String externalId;

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
    public ParkingLot(String externalId, String name, String address, Integer totalSpot,
                      Integer price, LocalTime operationStartTime, LocalTime operationEndTime) {
        this.externalId = externalId;
        this.name = name;
        this.address = address;
        this.totalSpot = totalSpot;
        this.price = price;
        this.operationStartTime = operationStartTime;
        this.operationEndTime = operationEndTime;
    }

    // [CUS-01] 외부 API에서 받은 값을 사용해 ParkingLot 엔티티를 생성하는 정적 팩토리 메서드
    public static ParkingLot of(String externalId, String name, String address, Integer totalSpot) {
        return ParkingLot.builder()
                .externalId(externalId)
                .name(name)
                .address(address)
                .totalSpot(totalSpot)
                .price(DEFAULT_PRICE)
                .operationStartTime(DEFAULT_OPERATION_START_TIME)
                .operationEndTime(DEFAULT_OPERATION_END_TIME)
                .build();
    }

    // [CUS-01] 외부 데이터 변경 시 업데이트
    public void updateInfo(String name, String address, Integer totalCapacity) {
        this.name = name;
        this.address = address;
        this.totalSpot = totalSpot;
    }
}