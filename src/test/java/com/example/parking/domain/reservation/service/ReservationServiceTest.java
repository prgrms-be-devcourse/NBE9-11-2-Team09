package com.example.parking.domain.reservation.service;

import com.example.parking.domain.parkingLot.entity.ParkingLot;
import com.example.parking.domain.parkingspot.entity.ParkingSpot;
import com.example.parking.domain.parkingspot.entity.SpotType;
import com.example.parking.domain.parkingLot.repository.ParkingLotRepository;
import com.example.parking.domain.parkingspot.repository.ParkingSpotRepository;
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
import java.util.List;

// assertj를 사용하면 테스트 검증 코드를 영어 문장처럼 직관적으로 읽히게 짤 수 있습니다.
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional // 💡 핵심: 테스트가 끝나면 DB에 넣었던 가짜 데이터를 모두 깔끔하게 롤백(삭제)해줍니다!
class ReservationServiceTest {

    @Autowired ReservationService reservationService;
    @Autowired ReservationRepository reservationRepository;

    // 외래 키(FK) 데이터를 넣기 위한 타 도메인 레포지토리
    @Autowired UserRepository userRepository;
    @Autowired ParkingLotRepository parkingLotRepository;
    @Autowired ParkingSpotRepository parkingSpotRepository;

    private User savedUser;
    private Reservation savedReservation;

    // @BeforeEach: 각각의 @Test 메서드가 실행되기 직전에 매번 무조건 실행되는 세팅 메서드입니다.
    @BeforeEach
    void setUp() {
        // 1. [민호님 도메인] 유저 가짜 데이터 생성 및 저장
        User user = User.builder()
                //initdata부분 때문에 이렇게 설정
                .email("test_test@test.com")
                .password("1234")
                .name("배재현_테스트")
                .plateNumber("12가3453")
                .vehicleType(VehicleType.SMALL)
                .role(UserRole.USER)
                .build();
        savedUser = userRepository.save(user);

        // 2. [지윤님 도메인] 주차장 가짜 데이터 생성 및 저장
        ParkingLot parkingLot = ParkingLot.builder()
                .name("강남역 공영 주차장")
                .address("서울시 강남구")
                .totalSpot(100)
                .price(5000)
                .operationStartTime(LocalTime.of(9, 0))
                .operationEndTime(LocalTime.of(22, 0))
                .build();
        ParkingLot savedLot = parkingLotRepository.save(parkingLot);

        // 3. [현태님 도메인] 주차 자리 가짜 데이터 생성 및 저장
        ParkingSpot parkingSpot = ParkingSpot.builder()
                .parkingLot(savedLot)
                .number("A-01")
                .type(SpotType.SMALL)
                .build();
        ParkingSpot savedSpot = parkingSpotRepository.save(parkingSpot);

        // 4. [재현님 도메인] 예약 가짜 데이터 생성 및 저장
        Reservation reservation = Reservation.builder()
                .user(savedUser)
                .parkingLot(savedLot)
                .parkingSpot(savedSpot)
                .startTime(LocalDateTime.now().plusHours(1)) // 1시간 뒤 시작
                .endTime(LocalDateTime.now().plusHours(3))   // 3시간 뒤 종료
                .build();
        savedReservation = reservationRepository.save(reservation);
    }

    @Test
    @DisplayName("[CUS-04] 내 예약 목록 조회가 정상적으로 작동해야 한다")
    void getMyReservations() {
        // given (주어진 상황: setUp에서 이미 가짜 데이터 1건이 세팅됨)

        // when (실행: 재현님이 작성한 서비스 로직을 호출한다)
        List<ReservationResDto> results = reservationService.getMyReservations(savedUser.getId(), null);

        // then (검증: 결과가 내 예상과 똑같은지 확인한다)
        assertThat(results).hasSize(1); // 1건이 나와야 함
        assertThat(results.get(0).parkingLotName()).isEqualTo("강남역 공영 주차장"); // 주차장 이름이 매핑되었는지 확인
        assertThat(results.get(0).parkingSpotNumber()).isEqualTo("A-01"); // 자리 번호가 매핑되었는지 확인
        assertThat(results.get(0).status()).isEqualTo(ReservationStatus.PENDING); // 상태 확인
    }

    @Test
    @DisplayName("[CUS-04] 예약을 취소하면 상태가 CANCELED로 변해야 한다")
    void cancelReservation() {
        // given (주어진 상황: 예약 아이디와 유저 아이디)
        Long targetReservationId = savedReservation.getId();
        Long requestUserId = savedUser.getId();

        // when (실행: 취소 로직 호출)
        reservationService.cancelReservation(targetReservationId, requestUserId);

        // then (검증: DB에서 해당 예약을 다시 꺼내서 상태가 변했는지 확인)
        Reservation canceledReservation = reservationRepository.findById(targetReservationId).get();

        assertThat(canceledReservation.getStatus()).isEqualTo(ReservationStatus.CANCELED);
        assertThat(canceledReservation.getCanceledAt()).isNotNull(); // 취소 시간이 기록되었는지 확인
    }
}