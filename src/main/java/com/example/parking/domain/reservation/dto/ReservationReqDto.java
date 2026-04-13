package com.example.parking.domain.reservation.dto;

import java.time.LocalDateTime;

public record ReservationReqDto(
        Long parkingLotId,
        Long parkingSpotId,
        LocalDateTime startTime,
        LocalDateTime endTime
) {}