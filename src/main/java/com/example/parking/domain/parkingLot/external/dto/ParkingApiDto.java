package com.example.parking.domain.parkingLot.external.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

// [CUS-01] 서울시 주차장 API 응답 DTO 묶음
public class ParkingApiDto {

    // 최상위 응답
    public record Response(
            @JsonProperty("GetParkInfo")
            ParkInfo parkInfo

    ) {
    }

    // 실제 데이터 영역
    public record ParkInfo(
            @JsonProperty("list_total_count")
            Integer listTotalCount,

            @JsonProperty("RESULT")
            ApiResult result,

            @JsonProperty("row")
            List<ParkingLotItem> items
    ) {
    }

    // 결과 코드
    public record ApiResult(
            @JsonProperty("CODE")
            String code,

            @JsonProperty("MESSAGE")
            String message
    ) {
    }

    // 개별 주차장 데이터
    public record ParkingLotItem(
            @JsonProperty("PKLT_CD")
            String pkltCd, // externalId

            @JsonProperty("PKLT_NM")
            String pkltNm, // name

            @JsonProperty("ADDR")
            String addr, // address

            @JsonProperty("TPKCT")
            Double tpkct // totalSpot
    ) {
    }
}
