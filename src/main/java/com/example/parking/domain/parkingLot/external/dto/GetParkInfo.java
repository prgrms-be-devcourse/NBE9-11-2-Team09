package com.example.parking.domain.parkingLot.external.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

// [CUS-01] 주차장 API의 실제 데이터 영역과 메타 정보를 담는 객체
public record GetParkInfo(

        @JsonProperty("list_total_count")
        Integer listTotalCount,

        @JsonProperty("RESULT")
        ParkingApiResult result,

        @JsonProperty("row")
        List<ParkingApiItem> row
) {
}
