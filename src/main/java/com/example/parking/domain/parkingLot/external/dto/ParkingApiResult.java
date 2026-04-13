package com.example.parking.domain.parkingLot.external.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

// [CUS-01] API 요청 결과 코드와 메시지를 담는 객체
public record ParkingApiResult(

        @JsonProperty("CODE")
        String code,

        @JsonProperty("MESSAGE")
        String message
) {
}
