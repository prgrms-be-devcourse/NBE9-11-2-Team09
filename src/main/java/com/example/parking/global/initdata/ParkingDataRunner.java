package com.example.parking.global.initdata;

import com.example.parking.domain.parkingLot.entity.ParkingLot;
import com.example.parking.domain.parkingLot.entity.SpotType;
import com.example.parking.domain.parkingLot.repository.ParkingLotRepository;
import com.example.parking.domain.parkingspot.entity.ParkingSpot;
import com.example.parking.domain.parkingspot.repository.ParkingSpotRepository;
import com.example.parking.domain.reservation.entity.Reservation;
import com.example.parking.domain.reservation.repository.ReservationRepository;
import com.example.parking.domain.user.entity.User;
import com.example.parking.domain.user.entity.UserRole;
import com.example.parking.domain.user.entity.VehicleType;
import com.example.parking.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
@RequiredArgsConstructor
public class ParkingDataRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ParkingLotRepository parkingLotRepository;
    private final ParkingSpotRepository parkingSpotRepository;
    private final ReservationRepository reservationRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) return; // 이미 데이터가 있으면 실행 안 함

        // 1. 유저 (ID: 1)
        User user = userRepository.save(User.builder().email("test@test.com").password("1234").name("배재현").plateNumber("12가3456").vehicleType(VehicleType.SMALL).role(UserRole.USER).build());

        // 2. 주차장 (ID: 1)
        ParkingLot parkingLot = parkingLotRepository.save(ParkingLot.builder().name("강남 공영 주차장").address("서울 강남구").totalSpot(50).price(3000).operationStartTime(LocalTime.of(9, 0)).operationEndTime(LocalTime.of(22, 0)).build());

        // 3. 주차 자리 (ID: 1)
        ParkingSpot parkingSpot = parkingSpotRepository.save(ParkingSpot.builder().parkingLot(parkingLot).number("A-01").type(SpotType.SMALL).build());

        // 4. 예약 (ID: 1)
        reservationRepository.save(Reservation.builder().user(user).parkingLot(parkingLot).parkingSpot(parkingSpot).startTime(LocalDateTime.now().plusHours(1)).endTime(LocalDateTime.now().plusHours(3)).build());
    }
}