package com.example.parking.domain.parkingLot.external;

import com.example.parking.domain.parkingLot.repository.ParkingLotRepository;
import com.example.parking.domain.parkingLot.service.ParkingLotSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ParkingLotInitRunner implements ApplicationRunner {

    private final ParkingLotSyncService parkingLotSyncService;
    private final ParkingLotRepository parkingLotRepository;

    // [CUS-01] 서버 시작 시 외부 주차장 데이터 동기화
    // 데이터가 비어 있을 때만 초기 적재하도록 처리
    @Override
    public void run(ApplicationArguments args) {
        if (parkingLotRepository.count() == 0) {
            log.info("주차장 초기 데이터 동기화를 시작합니다.");
            parkingLotSyncService.syncParkingLots();
            log.info("주차장 초기 데이터 동기화가 완료되었습니다.");
        } else {
            log.info("주차장 데이터가 이미 존재하여 초기 동기화를 생략합니다.");
        }
    }
}
