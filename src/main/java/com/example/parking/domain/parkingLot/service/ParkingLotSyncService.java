package com.example.parking.domain.parkingLot.service;

import com.example.parking.domain.parkingLot.entity.ParkingLot;
import com.example.parking.domain.parkingLot.external.ParkingOpenApiClient;
import com.example.parking.domain.parkingLot.external.dto.ParkingApiDto;
import com.example.parking.domain.parkingLot.repository.ParkingLotRepository;
import com.example.parking.domain.parkingspot.service.ParkingSpotService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
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
    private final ParkingSpotService parkingSpotService;

    // [CUS-01] 외부 주차장 데이터를 우리 DB와 동기화
    @CacheEvict(value = {"parkingLots", "parkingLot"}, allEntries = true)
    public void syncParkingLots() {
        ParkingApiDto.Response response = parkingOpenApiClient.fetchParkingLots();
        ParkingApiDto.ParkInfo parkInfo = response.parkInfo();

        for (ParkingApiDto.ParkingLotItem item : parkInfo.items()) {
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
                            () -> {
                                ParkingLot saved = parkingLotRepository.save(
                                        ParkingLot.of(externalId, name, address, totalSpot)
                                );
                                // 신규 주차장일 때만 자리 생성
                                if (totalSpot != null && totalSpot > 0) {
                                    parkingSpotService.createSpots(saved, totalSpot);
                                }
                            }
                    );
        }
    }
}