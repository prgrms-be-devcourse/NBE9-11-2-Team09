package com.example.parking.domain.parkingLot.external.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

// 서울시 주차장 API의 최상위 응답 객체
public record ParkingApiResDto(

        // JSON의 "GetParkInfo" 필드와 매핑
        @JsonProperty("GetParkInfo")
        GetParkInfo getParkInfo

) {
}
