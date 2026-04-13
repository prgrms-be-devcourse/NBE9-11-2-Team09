package com.example.parking.domain.reservation.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

// [CUS-03] 예약 생성 - 프론트엔드로부터 받을 데이터 양식
@Getter
@NoArgsConstructor
public class ReservationReqDto {
    private Long parkingLotId;
    private Long parkingSpotId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}