package com.example.parking.domain.parkingLot.external.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

// [CUS-01] 개별 주차장 정보를 담는 외부 API 데이터 객체
public record ParkingApiItem(

        @JsonProperty("PKLT_CD")
        String pkltCd,

        @JsonProperty("PKLT_NM")
        String pkltNm,

        @JsonProperty("ADDR")
        String addr,

        @JsonProperty("TPKCT")
        Double tpkct
) {
}
