package com.example.parking.domain.parkingLot.external.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ParkingApiItem(

        @JsonProperty("pklt_cd")
        String pkltCd,

        @JsonProperty("pklt_nm")
        String pkltNm,

        @JsonProperty("addr")
        String addr,

        @JsonProperty("tpkct")
        Integer tpkct
) {
}
