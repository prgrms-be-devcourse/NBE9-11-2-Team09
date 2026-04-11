package com.example.parking.domain.parkingLot.dto;

public record ParkingLotResDto(
        String externalId,
        String name,
        String address,
        Integer totalSpot
) {
    public static ParkingLotResDto from(ParkingApiItem item) {
        return new ParkingLotResDto(
                item.pkltCd(),
                item.pkltNm(),
                item.addr(),
                item.tpkct()
        );
    }
}
