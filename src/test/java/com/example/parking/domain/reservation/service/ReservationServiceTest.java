package com.example.parking.domain.reservation.service;

import com.example.parking.domain.parkingLot.entity.ParkingLot;
import com.example.parking.domain.parkingLot.repository.ParkingLotRepository;
import com.example.parking.domain.parkingspot.entity.ParkingSpot;
import com.example.parking.domain.parkingspot.entity.SpotStatus;
import com.example.parking.domain.parkingspot.entity.SpotType;
import com.example.parking.domain.parkingspot.repository.ParkingSpotRepository;
import com.example.parking.domain.reservation.dto.ReservationReqDto;
import com.example.parking.domain.reservation.dto.ReservationResDto;
import com.example.parking.domain.reservation.entity.Reservation;
import com.example.parking.domain.reservation.entity.ReservationStatus;
import com.example.parking.domain.reservation.repository.ReservationRepository;
import com.example.parking.domain.user.entity.User;
import com.example.parking.domain.user.entity.UserRole;
import com.example.parking.domain.user.entity.VehicleType;
import com.example.parking.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class ReservationServiceTest {

    @Autowired ReservationService reservationService;
    @Autowired ReservationRepository reservationRepository;
    @Autowired UserRepository userRepository;
    @Autowired ParkingLotRepository parkingLotRepository;
    @Autowired ParkingSpotRepository parkingSpotRepository;

    private User savedUser;
    private ParkingLot savedLot;
    private ParkingSpot savedSpot;
    private Reservation savedReservation;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .email("test_test@test.com")
                .password("1234")
                .name("테스트유저")
                .plateNumber("12가3453")
                .vehicleType(VehicleType.SMALL)
                .role(UserRole.USER)
                .build();
        savedUser = userRepository.save(user);

        ParkingLot parkingLot = ParkingLot.builder()
                .name("강남역 공영 주차장")
                .address("서울시 강남구")
                .totalSpot(100)
                .price(5000)
                .externalId("TEST_001")
                .operationStartTime(LocalTime.of(9, 0))
                .operationEndTime(LocalTime.of(22, 0))
                .build();
        savedLot = parkingLotRepository.save(parkingLot);

        ParkingSpot parkingSpot = ParkingSpot.create(savedLot, "A-01", SpotType.SMALL);
        savedSpot = parkingSpotRepository.save(parkingSpot);

        Reservation reservation = Reservation.builder()
                .user(savedUser)
                .parkingLot(savedLot)
                .parkingSpot(savedSpot)
                .startTime(LocalDateTime.now().plusHours(2))
                .endTime(LocalDateTime.now().plusHours(4))
                .status(ReservationStatus.PENDING)
                .build();
        savedReservation = reservationRepository.save(reservation);
    }

    @Test
    @DisplayName("[CUS-04] 내 예약 목록 조회 - 정상적으로 조회되어야 한다")
    void getMyReservations_success() {
        List<ReservationResDto> results = reservationService.getMyReservations(savedUser.getId(), null);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).parkingLotName()).isEqualTo("강남역 공영 주차장");
        assertThat(results.get(0).parkingSpotNumber()).isEqualTo("A-01");
        assertThat(results.get(0).status()).isEqualTo(ReservationStatus.PENDING);
    }

    @Test
    @DisplayName("[CUS-04] 내 예약 목록 조회 - 상태 필터링이 동작해야 한다")
    void getMyReservations_filterByStatus() {
        List<ReservationResDto> pendingResults = reservationService.getMyReservations(savedUser.getId(), ReservationStatus.PENDING);
        List<ReservationResDto> confirmedResults = reservationService.getMyReservations(savedUser.getId(), ReservationStatus.CONFIRMED);

        assertThat(pendingResults).hasSize(1);
        assertThat(confirmedResults).hasSize(0);
    }

    @Test
    @DisplayName("[CUS-04] 예약 상세 조회 - 정상적으로 조회되어야 한다")
    void getReservationDetail_success() {
        ReservationResDto result = reservationService.getReservationDetail(savedReservation.getId(), savedUser.getId());

        assertThat(result.reservationId()).isEqualTo(savedReservation.getId());
        assertThat(result.parkingLotName()).isEqualTo("강남역 공영 주차장");
        assertThat(result.status()).isEqualTo(ReservationStatus.PENDING);
    }

    @Test
    @DisplayName("[CUS-04] 예약 상세 조회 - 다른 유저의 예약 조회 시 예외가 발생해야 한다")
    void getReservationDetail_otherUser_fail() {
        User otherUser = User.builder()
                .email("other@test.com")
                .password("1234")
                .name("다른유저")
                .plateNumber("99가9999")
                .vehicleType(VehicleType.SMALL)
                .role(UserRole.USER)
                .build();
        User savedOtherUser = userRepository.save(otherUser);

        assertThatThrownBy(() ->
                reservationService.getReservationDetail(savedReservation.getId(), savedOtherUser.getId())
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않거나 권한이 없는 예약입니다.");
    }

    @Test
    @DisplayName("[CUS-03] 예약 생성 - 정상적으로 생성되어야 한다")
    void createReservation_success() {
        User newUser = User.builder()
                .email("new_test@test.com")
                .password("1234")
                .name("새테스트유저")
                .plateNumber("34나5678")
                .vehicleType(VehicleType.SMALL)
                .role(UserRole.USER)
                .build();
        User savedNewUser = userRepository.save(newUser);

        ParkingSpot newSpot = ParkingSpot.create(savedLot, "A-02", SpotType.SMALL);
        ParkingSpot savedNewSpot = parkingSpotRepository.save(newSpot);

        ReservationReqDto reqDto = new ReservationReqDto(
                savedLot.getId(),
                savedNewSpot.getId(),
                LocalDateTime.now().plusHours(5).format(FORMATTER),
                LocalDateTime.now().plusHours(7).format(FORMATTER)
        );

        ReservationResDto result = reservationService.createReservation(savedNewUser.getId(), reqDto);

        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(ReservationStatus.PENDING);
        assertThat(result.parkingLotName()).isEqualTo("강남역 공영 주차장");

        ParkingSpot updatedSpot = parkingSpotRepository.findById(savedNewSpot.getId()).get();
        assertThat(updatedSpot.getStatus()).isEqualTo(SpotStatus.OCCUPIED);
    }

    @Test
    @DisplayName("[CUS-03] 예약 생성 - 과거 시간으로 예약 시 예외가 발생해야 한다")
    void createReservation_pastTime_fail() {
        ReservationReqDto reqDto = new ReservationReqDto(
                savedLot.getId(),
                savedSpot.getId(),
                LocalDateTime.now().minusHours(2).format(FORMATTER),
                LocalDateTime.now().minusHours(1).format(FORMATTER)
        );

        assertThatThrownBy(() ->
                reservationService.createReservation(savedUser.getId(), reqDto)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("과거 시간으로 예약할 수 없습니다.");
    }

    @Test
    @DisplayName("[CUS-03] 예약 생성 - 종료 시간이 시작 시간보다 앞서면 예외가 발생해야 한다")
    void createReservation_endBeforeStart_fail() {
        ReservationReqDto reqDto = new ReservationReqDto(
                savedLot.getId(),
                savedSpot.getId(),
                LocalDateTime.now().plusHours(5).format(FORMATTER),
                LocalDateTime.now().plusHours(3).format(FORMATTER)
        );

        assertThatThrownBy(() ->
                reservationService.createReservation(savedUser.getId(), reqDto)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("종료 시간이 시작 시간보다 앞설 수 없습니다.");
    }

    @Test
    @DisplayName("[CUS-03] 예약 생성 - 차종이 다른 자리 예약 시 예외가 발생해야 한다")
    void createReservation_vehicleTypeMismatch_fail() {
        ParkingSpot largeSpot = ParkingSpot.create(savedLot, "B-01", SpotType.LARGE);
        ParkingSpot savedLargeSpot = parkingSpotRepository.save(largeSpot);

        ReservationReqDto reqDto = new ReservationReqDto(
                savedLot.getId(),
                savedLargeSpot.getId(),
                LocalDateTime.now().plusHours(5).format(FORMATTER),
                LocalDateTime.now().plusHours(7).format(FORMATTER)
        );

        assertThatThrownBy(() ->
                reservationService.createReservation(savedUser.getId(), reqDto)
        ).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이용할 수 없습니다.");
    }

    @Test
    @DisplayName("[CUS-03] 예약 생성 - 같은 주차장에 이미 진행 중인 예약이 있으면 예외가 발생해야 한다")
    void createReservation_sameUserSameLot_fail() {
        ParkingSpot newSpot = ParkingSpot.create(savedLot, "A-06", SpotType.SMALL);
        ParkingSpot savedNewSpot = parkingSpotRepository.save(newSpot);

        ReservationReqDto reqDto = new ReservationReqDto(
                savedLot.getId(),
                savedNewSpot.getId(),
                LocalDateTime.now().plusHours(5).format(FORMATTER),
                LocalDateTime.now().plusHours(7).format(FORMATTER)
        );

        assertThatThrownBy(() ->
                reservationService.createReservation(savedUser.getId(), reqDto)
        ).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 이 주차장에 진행 중인 예약(또는 선점)이 존재합니다. 1주차장 당 1자리만 이용 가능합니다.");
    }

    @Test
    @DisplayName("[CUS-04] 예약 취소 - 정상적으로 취소되어야 한다")
    void cancelReservation_success() {
        reservationService.cancelReservation(savedReservation.getId(), savedUser.getId(), false);

        Reservation canceled = reservationRepository.findById(savedReservation.getId()).get();
        assertThat(canceled.getStatus()).isEqualTo(ReservationStatus.CANCELED);
        assertThat(canceled.getCanceledAt()).isNotNull();
    }

    @Test
    @DisplayName("[CUS-04] 예약 취소 - 이미 취소된 예약은 예외가 발생해야 한다")
    void cancelReservation_alreadyCanceled_fail() {
        reservationService.cancelReservation(savedReservation.getId(), savedUser.getId(), false);

        assertThatThrownBy(() ->
                reservationService.cancelReservation(savedReservation.getId(), savedUser.getId(), false)
        ).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 취소 처리된 예약입니다.");
    }

    @Test
    @DisplayName("[CUS-04] 예약 취소 - 다른 유저의 예약 취소 시 예외가 발생해야 한다")
    void cancelReservation_otherUser_fail() {
        User otherUser = User.builder()
                .email("other@test.com")
                .password("1234")
                .name("다른유저")
                .plateNumber("99가9999")
                .vehicleType(VehicleType.SMALL)
                .role(UserRole.USER)
                .build();
        User savedOtherUser = userRepository.save(otherUser);

        assertThatThrownBy(() ->
                reservationService.cancelReservation(savedReservation.getId(), savedOtherUser.getId(), false)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("해당 예약을 취소할 권한이 없습니다.");
    }

    @Test
    @DisplayName("[CUS-04] 예약 취소 - 입차 30분 전 이후에는 취소할 수 없다")
    void cancelReservation_after30Minutes_fail() {
        ParkingSpot newSpot = ParkingSpot.create(savedLot, "A-04", SpotType.SMALL);
        ParkingSpot savedNewSpot = parkingSpotRepository.save(newSpot);

        Reservation soonReservation = Reservation.builder()
                .user(savedUser)
                .parkingLot(savedLot)
                .parkingSpot(savedNewSpot)
                .startTime(LocalDateTime.now().plusMinutes(10))
                .endTime(LocalDateTime.now().plusHours(2))
                .status(ReservationStatus.PENDING)
                .build();
        Reservation savedSoonReservation = reservationRepository.save(soonReservation);

        assertThatThrownBy(() ->
                reservationService.cancelReservation(savedSoonReservation.getId(), savedUser.getId(), false)
        ).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("입차 30분 전까지만 취소가 가능합니다.");
    }

    @Test
    @DisplayName("[ADM] 관리자 강제 취소 - isForced=true이면 제한 없이 취소되어야 한다")
    void cancelReservation_forced_success() {
        ParkingSpot newSpot = ParkingSpot.create(savedLot, "A-05", SpotType.SMALL);
        ParkingSpot savedNewSpot = parkingSpotRepository.save(newSpot);

        Reservation soonReservation = Reservation.builder()
                .user(savedUser)
                .parkingLot(savedLot)
                .parkingSpot(savedNewSpot)
                .startTime(LocalDateTime.now().plusMinutes(10))
                .endTime(LocalDateTime.now().plusHours(2))
                .status(ReservationStatus.PENDING)
                .build();
        Reservation savedSoonReservation = reservationRepository.save(soonReservation);

        reservationService.cancelReservation(savedSoonReservation.getId(), null, true);

        Reservation canceled = reservationRepository.findById(savedSoonReservation.getId()).get();
        assertThat(canceled.getStatus()).isEqualTo(ReservationStatus.CANCELED);
    }
}