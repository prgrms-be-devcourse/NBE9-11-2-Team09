package com.example.parking.domain.parkingLot.external.dto;

/*
외부 API에서 받아온 정보를 우리 서비스에서 사용하기 위한 형태로 변환한 내부 DTO

- ParkingApiItem(외부 DTO)을 기반으로 생성됨
- 외부 API에 의존하지 않을 수 있음
 */
public record ParkingLotDto(
        String externalId,
        String name,
        String address,
        Integer totalSpot
) {
    // 외부 API DTO(ParkingApiItem)를 내부 DTO로 변환하는 정적 팩토리 메서드
    public static ParkingLotDto from(ParkingApiItem item) {
        return new ParkingLotDto(
                item.pkltCd(),
                item.pkltNm(),
                item.addr(),
                item.tpkct()
        );
    }
}
