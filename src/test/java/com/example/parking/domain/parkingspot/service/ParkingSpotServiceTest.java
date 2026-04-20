package com.example.parking.domain.parkingspot.service;

import com.example.parking.domain.parkingLot.entity.ParkingLot;
import com.example.parking.domain.parkingLot.repository.ParkingLotRepository;
import com.example.parking.domain.parkingspot.dto.ParkingSpotDto;
import com.example.parking.domain.parkingspot.entity.ParkingSpot;
import com.example.parking.domain.parkingspot.entity.SpotStatus;
import com.example.parking.domain.parkingspot.entity.SpotType;
import com.example.parking.domain.parkingspot.repository.ParkingSpotRepository;
import com.example.parking.domain.parkingspot.scheduler.ParkingSpotScheduler;
import com.example.parking.domain.payment.scheduler.PaymentScheduler;
import com.example.parking.domain.reservation.scheduler.ReservationScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
public class ParkingSpotServiceTest {
  @MockitoBean
  ParkingSpotScheduler parkingSpotScheduler;   // ✅ 스케줄러 비활성화
  @MockitoBean
  PaymentScheduler paymentScheduler;
  @MockitoBean
  ReservationScheduler reservationScheduler;
  @Autowired
  ParkingSpotRepository parkingSpotRepository;
  @Autowired
  ParkingLotRepository parkingLotRepository;
  @Autowired
  ParkingSpotService parkingSpotService;

  private ParkingSpot savedSpot;
  private ParkingLot savedLot;

  @BeforeEach
  void setUp() {
    ParkingLot parkingLot = ParkingLot.builder()
        .externalId("test-lot-001")
        .name("테스트 주차장")
        .address("서울시 테스트구")
        .totalSpot(10)
        .price(1000)
        .operationStartTime(LocalTime.of(0, 0))
        .operationEndTime(LocalTime.of(23, 59))
        .build();
    savedLot = parkingLotRepository.save(parkingLot);

    ParkingSpot spot = ParkingSpot.create(savedLot, "A-01", SpotType.SMALL);
    savedSpot = parkingSpotRepository.save(spot);
  }

  @Test
  @DisplayName("AVAILABLE 자리에 tryReserve 호출 시 반환값이 1이고 상태가 OCCUPIED로 바뀐다")
  void tryReserve_success() {
    // given
    LocalDateTime now = LocalDateTime.now();

    // when
    int result = parkingSpotRepository.tryReserve(savedSpot.getId(), now);

    // then
    assertThat(result).isEqualTo(1);

    ParkingSpot updated = parkingSpotRepository.findById(savedSpot.getId()).get();
    assertThat(updated.getStatus()).isEqualTo(SpotStatus.OCCUPIED);
    assertThat(updated.getReservedAt()).isNotNull();
  }

  @Test
  @DisplayName("이미 OCCUPIED인 자리에 tryReserve 호출 시 반환값이 0이고 상태가 변하지 않는다")
  void tryReserve_fail_alreadyOccupied() {
    // given - 먼저 한 번 선점
    parkingSpotRepository.tryReserve(savedSpot.getId(), LocalDateTime.now());

    // when - 이미 점유된 자리에 다시 시도
    int result = parkingSpotRepository.tryReserve(savedSpot.getId(), LocalDateTime.now());

    // then
    assertThat(result).isEqualTo(0);

    ParkingSpot unchanged = parkingSpotRepository.findById(savedSpot.getId()).get();
    assertThat(unchanged.getStatus()).isEqualTo(SpotStatus.OCCUPIED);
  }

  @Test
  @DisplayName("특정 주차장의 AVAILABLE 자리만 반환한다")
  void findAvailableSpots_onlyAvailable() {
    // given - AVAILABLE 1개, OCCUPIED 1개 저장
    ParkingSpot available = ParkingSpot.create(savedLot, "A-03", SpotType.SMALL);
    ParkingSpot occupied  = ParkingSpot.create(savedLot, "A-04", SpotType.SMALL);
    occupied.reserve(); // OCCUPIED 상태로
    parkingSpotRepository.saveAll(List.of(available, occupied));

    // when
    List<ParkingSpotDto> result = parkingSpotService.findAvailableSpots(savedLot.getId());

    // then
    assertThat(result).hasSize(2);
    assertThat(result.get(0).status()).isEqualTo(SpotStatus.AVAILABLE);
  }

  @Test
  @DisplayName("totalSpot 개수만큼 자리가 생성된다")
  void createSpots_totalCount() {
    // given - setUp에서 만든 자리 1개만 제거 (FK 문제 없음)
    parkingSpotRepository.delete(savedSpot);
    int totalSpot = 10;

    // when
    parkingSpotService.createSpots(savedLot, totalSpot);

    // then
    List<ParkingSpot> spots = parkingSpotRepository.findAll().stream()
        .filter(s -> s.getParkingLot().getId().equals(savedLot.getId()))
        .toList();
    assertThat(spots).hasSize(10);
  }

  @Test
  @DisplayName("자리 타입이 SMALL 80%, LARGE 10%, ELECTRIC 10% 비율로 생성된다")
  void createSpots_typeRatio() {
    // given - setUp에서 만든 자리 1개만 제거
    parkingSpotRepository.delete(savedSpot);
    int totalSpot = 10;

    // when
    parkingSpotService.createSpots(savedLot, totalSpot);

    // then
    List<ParkingSpot> spots = parkingSpotRepository.findAll().stream()
        .filter(s -> s.getParkingLot().getId().equals(savedLot.getId()))
        .toList();

    long smallCount    = spots.stream().filter(s -> s.getType() == SpotType.SMALL).count();
    long largeCount    = spots.stream().filter(s -> s.getType() == SpotType.LARGE).count();
    long electricCount = spots.stream().filter(s -> s.getType() == SpotType.ELECTRIC).count();

    assertThat(smallCount).isEqualTo(8);
    assertThat(largeCount).isEqualTo(1);
    assertThat(electricCount).isEqualTo(1);
  }

  @Test
  @DisplayName("관리자가 자리 상태를 원하는 값으로 변경할 수 있다")
  void updateSpotStatusByAdmin_success() {
    // given
    ParkingSpot spot = ParkingSpot.create(savedLot, "A-01", SpotType.SMALL);
    ParkingSpot saved = parkingSpotRepository.save(spot);

    // when
    parkingSpotService.updateSpotStatusByAdmin(saved.getId(), SpotStatus.OCCUPIED);

    // then
    ParkingSpot updated = parkingSpotRepository.findById(saved.getId()).get();
    assertThat(updated.getStatus()).isEqualTo(SpotStatus.OCCUPIED);
  }

  @Test
  @DisplayName("존재하지 않는 spotId로 요청하면 예외가 발생한다")
  void updateSpotStatusByAdmin_notFound() {
    // given
    Long invalidId = 999999L;

    // when & then
    assertThatThrownBy(() -> parkingSpotService.updateSpotStatusByAdmin(invalidId, SpotStatus.OCCUPIED))
        .isInstanceOf(Exception.class); // 실제 예외 타입에 맞게 조정
  }

  @Test
  @DisplayName("subscribe 호출 시 SseEmitter가 반환된다")
  void subscribe_returnsSseEmitter() {
    // given
    Long parkingLotId = savedLot.getId();

    // when
    SseEmitter emitter = parkingSpotService.subscribe(parkingLotId);

    // then
    assertThat(emitter).isNotNull();
  }

}
