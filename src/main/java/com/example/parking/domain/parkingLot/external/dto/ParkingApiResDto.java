package com.example.parking.domain.parkingLot.external.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/*
외부 공공데이터 API의 최상위 JSON 응답을 매핑하는 DTO

- 응답을 "DESCRIPTION(메타데이터)"와 "DATA(실제 데이터)"로 나누어 반환하는 구조
- 실제 서비스에서는 DATA 영역만 사용하며, DESCRIPTION은 구조 보존을 위해 Map 형태로만 받아옴
 */

public record ParkingApiResDto(

        // JSON의 "DESCRIPTION" 필드와 매핑
        @JsonProperty("DESCRIPTION")
        Map<String, String> description,

        // JSON의 "DATA" 필드와 매핑
        @JsonProperty("DATA")
        List<ParkingApiItem> data

) {
}
