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

    @Override
    public void run(ApplicationArguments args) {
        // 서버 시작할 때마다 무조건 sync하면 중복/불필요 호출이 생길 수 있어서
        // 데이터가 비어 있을 때만 초기 적재하도록 처리
        if (parkingLotRepository.count() == 0) {
            log.info("주차장 초기 데이터 동기화를 시작합니다.");
            parkingLotSyncService.syncParkingLots();
            log.info("주차장 초기 데이터 동기화가 완료되었습니다.");
        } else {
            log.info("주차장 데이터가 이미 존재하여 초기 동기화를 생략합니다.");
        }
    }
}
