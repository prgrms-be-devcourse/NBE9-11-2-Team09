package com.example.parking.domain.parkingLot.service;

import com.example.parking.domain.parkingLot.entity.ParkingLot;
import com.example.parking.domain.parkingLot.external.ParkingOpenApiClient;
import com.example.parking.domain.parkingLot.external.dto.GetParkInfo;
import com.example.parking.domain.parkingLot.external.dto.ParkingApiItem;
import com.example.parking.domain.parkingLot.external.dto.ParkingApiResDto;
import com.example.parking.domain.parkingLot.repository.ParkingLotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/*
주차장 외부 공공데이터를 우리 DB와 동기화하는 서비스 클래스

- 외부 API로부터 주차장 목록 조회
- 외부 API의 고유 식별자(externalId)로 기존 데이터 조회
- 기존 데이터가 없으면 신규 생성
- 기존 데이터가 있으면 최신 정보로 수정
- 최종적으로 DB에 반영
*/
@Service
@RequiredArgsConstructor
@Transactional
public class ParkingLotSyncService {

    private final ParkingOpenApiClient parkingOpenApiClient;
    private final ParkingLotRepository parkingLotRepository;

    // [CUS-01] 외부 주차장 데이터를 우리 DB와 동기화
    public void syncParkingLots() {
        ParkingApiResDto response = parkingOpenApiClient.fetchParkingLots();

        if (response == null || response.getParkInfo() == null) {
            return;
        }

        GetParkInfo parkInfo = response.getParkInfo();

        if (parkInfo.row() == null || parkInfo.row().isEmpty()) {
            return;
        }

        for (ParkingApiItem item : parkInfo.row()) {
            String externalId = item.pkltCd();

            if (externalId == null || externalId.isBlank()) {
                continue;
            }

            String name = item.pkltNm();
            String address = item.addr();
            Integer totalSpot = item.tpkct() == null ? null : item.tpkct().intValue();

            parkingLotRepository.findByExternalId(externalId)
                    .ifPresentOrElse(
                            parkingLot -> parkingLot.updateInfo(name, address, totalSpot),
                            () -> parkingLotRepository.save(
                                    ParkingLot.of(externalId, name, address, totalSpot)
                            )
                    );
        }
    }
}