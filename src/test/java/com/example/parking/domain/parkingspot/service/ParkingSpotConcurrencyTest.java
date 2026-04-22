package com.example.parking.domain.parkingspot.service;

import com.example.parking.domain.parkingLot.entity.ParkingLot;
import com.example.parking.domain.parkingLot.repository.ParkingLotRepository;
import com.example.parking.domain.parkingspot.entity.ParkingSpot;
import com.example.parking.domain.parkingspot.entity.SpotType;
import com.example.parking.domain.parkingspot.repository.ParkingSpotRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

// [CUS-11] 동시성 테스트
@SpringBootTest
public class ParkingSpotConcurrencyTest {

  @Autowired
  private ParkingSpotRepository parkingSpotRepository;
  @Autowired
  private ParkingLotRepository parkingLotRepository;

  // 추가: 데이터를 실제로 DB에 밀어넣기 위해 주입
  @Autowired
  private TransactionTemplate transactionTemplate;

  private Long savedSpotId;

  @BeforeEach
  void setUp() {
    // transactionTemplate을 사용하여 데이터를 DB에 실제로 커밋
    savedSpotId = transactionTemplate.execute(status -> {
      ParkingLot parkingLot = ParkingLot.builder()
          .externalId("test-lot-" + UUID.randomUUID())
          .name("테스트 주차장")
          .address("서울시 테스트구")
          .totalSpot(10)
          .price(1000)
          .operationStartTime(LocalTime.of(0, 0))
          .operationEndTime(LocalTime.of(23, 59))
          .build();
      ParkingLot savedLot = parkingLotRepository.save(parkingLot);

      ParkingSpot spot = ParkingSpot.create(savedLot, "A-01", SpotType.SMALL);
      return parkingSpotRepository.save(spot).getId();
    });
  }

  @Test
  @DisplayName("동시에 두 요청이 같은 자리에 tryReserve를 호출하면 하나만 성공한다")
  void tryReserve_concurrency() throws InterruptedException {
    int threadCount = 2;
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    List<Integer> results = Collections.synchronizedList(new ArrayList<>());
    CountDownLatch latch = new CountDownLatch(threadCount);

    for (int i = 0; i < threadCount; i++) {
      executor.submit(() -> {
        try {
          // [디버깅용] 데이터 존재 여부 확인
          System.out.println("Thread accessing ID: " + savedSpotId);

          // tryReserve는 @Modifying 쿼리이므로 별도의 트랜잭션이 필요할 수 있음
          int result = transactionTemplate.execute(status ->
              parkingSpotRepository.tryReserve(savedSpotId, LocalDateTime.now())
          );
          results.add(result);
        } catch (Exception e) {
          e.printStackTrace(); // 에러 발생 시 로그 출력
        } finally {
          latch.countDown();
        }
      });
    }

    latch.await(10, TimeUnit.SECONDS);

    // 검증: 비어있지 않은지 먼저 확인
    assertThat(results).as("결과 리스트가 비어있습니다. DB 연결이나 데이터 생성 확인 필요")
        .isNotEmpty();
    assertThat(results).containsExactlyInAnyOrder(0, 1);
  }

  @AfterEach
  void tearDown() {
    // 수동 삭제
    if (savedSpotId != null) {
      parkingSpotRepository.findById(savedSpotId).ifPresent(spot -> {
        ParkingLot lot = spot.getParkingLot();
        parkingSpotRepository.delete(spot);
        parkingLotRepository.delete(lot);
      });
    }
  }

  @Test
  @DisplayName("강남구 트래픽 - 100명이 동시에 같은 자리를 선점 시도하면 단 1명만 성공한다")
  void tryReserve_gangnamTraffic() throws InterruptedException {
    // given - 강남 주차장 자리 1개 생성
    Long spotId = transactionTemplate.execute(status -> {
      ParkingLot gangnamLot = ParkingLot.builder()
          .externalId("gangnam-lot-" + UUID.randomUUID())
          .name("강남역 공영주차장")
          .address("서울시 강남구 강남대로")
          .totalSpot(100)
          .price(5000)
          .operationStartTime(LocalTime.of(0, 0))
          .operationEndTime(LocalTime.of(23, 59))
          .build();
      ParkingLot savedLot = parkingLotRepository.save(gangnamLot);
      ParkingSpot spot = ParkingSpot.create(savedLot, "G-01", SpotType.SMALL);
      return parkingSpotRepository.save(spot).getId();
    });

    int threadCount = 100; // 강남구 동시 트래픽 가정
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    List<Integer> results = Collections.synchronizedList(new ArrayList<>());
    CountDownLatch ready = new CountDownLatch(threadCount); // 모든 스레드 준비 완료 대기
    CountDownLatch start = new CountDownLatch(1);           // 동시 출발 신호
    CountDownLatch done  = new CountDownLatch(threadCount); // 모든 스레드 완료 대기

    // when - 100개 스레드가 동시에 출발
    for (int i = 0; i < threadCount; i++) {
      executor.submit(() -> {
        try {
          ready.countDown();    // 준비 완료 신호
          start.await();        // 출발 신호 대기 (진짜 동시 요청)
          Integer result = transactionTemplate.execute(s ->
              parkingSpotRepository.tryReserve(spotId, LocalDateTime.now())
          );
          results.add(result);
        } catch (Exception e) {
          e.printStackTrace();
        } finally {
          done.countDown();
        }
      });
    }

    ready.await();          // 100개 스레드 모두 준비될 때까지 대기
    start.countDown();      // 동시 출발
    done.await(30, TimeUnit.SECONDS);

    // then
    long successCount = results.stream().filter(r -> r == 1).count();
    long failCount    = results.stream().filter(r -> r == 0).count();

    System.out.println("✅ 성공: " + successCount + "건, ❌ 실패: " + failCount + "건");

    assertThat(successCount).isEqualTo(1);   // 단 1명만 선점 성공
    assertThat(failCount).isEqualTo(99);     // 나머지 99명은 실패
    assertThat(results).hasSize(100);        // 100개 모두 응답받음
  }

}