package com.example.parking.domain.parkingLot.external.dto;

// 외부 API 데이터를 내부 서비스에서 사용하는 형태로 변환한 객체
public record ParkingLotDto(
        String externalId,
        String name,
        String address,
        Integer totalSpot
) {
    public static ParkingLotDto from(ParkingApiItem item) {
        return new ParkingLotDto(
                item.pkltCd(),
                item.pkltNm(),
                item.addr(),
                item.tpkct() == null ? null : item.tpkct().intValue()
        );
    }
}
