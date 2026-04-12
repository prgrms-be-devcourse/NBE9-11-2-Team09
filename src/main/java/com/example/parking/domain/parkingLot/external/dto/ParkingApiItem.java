package com.example.parking.domain.parkingLot.external.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/*
외부 API의 DATA 영역에 포함된 개별 주차장 정보를 매핑하는 DTO

- 서비스에 실제로 사용되는 필드만 가져옴
- 이후 내부 서비스용 DTO(ParkingLotDto)로 변환되어 사용
 */
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
