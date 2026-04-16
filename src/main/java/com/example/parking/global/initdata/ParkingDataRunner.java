//package com.example.parking.global.initdata;
//
//import com.example.parking.domain.parkingLot.entity.ParkingLot;
//import com.example.parking.domain.parkingLot.repository.ParkingLotRepository;
//import com.example.parking.domain.parkingspot.entity.ParkingSpot;
//import com.example.parking.domain.parkingspot.entity.SpotType;
//import com.example.parking.domain.parkingspot.repository.ParkingSpotRepository;
//import com.example.parking.domain.reservation.entity.Reservation;
//import com.example.parking.domain.reservation.entity.ReservationStatus;
//import com.example.parking.domain.reservation.repository.ReservationRepository;
//import com.example.parking.domain.user.entity.UserRole;
//import com.example.parking.domain.user.entity.VehicleType;
//import com.example.parking.domain.user.repository.UserRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//import com.example.parking.domain.user.entity.User;
//
//
//import java.time.LocalDateTime;
//import java.time.LocalTime;
//
//@Component
//@RequiredArgsConstructor
//public class ParkingDataRunner implements CommandLineRunner {
//
//    private final UserRepository userRepository;
//    private final ParkingLotRepository parkingLotRepository;
//    private final ParkingSpotRepository parkingSpotRepository;
//    private final ReservationRepository reservationRepository;
//    private final PasswordEncoder passwordEncoder;
//
//    @Override
//    @Transactional
//    public void run(String... args) {
//        if (userRepository.count() > 0) return;
//
//        // 1. 유저
//        User user = userRepository.save(User.builder()
//                .email("test@test.com")
//                .password(passwordEncoder.encode("1234"))
//                .name("배재현")
//                .plateNumber("12가3456")
//                .vehicleType(VehicleType.SMALL)
//                .role(UserRole.USER)
//                .build());
//
//        // 2. 주차장
//        ParkingLot parkingLot = parkingLotRepository.save(ParkingLot.builder()
//                .name("강남 공영 주차장")
//                .address("서울 강남구")
//                .totalSpot(50)
//                .price(3000)
//                .operationStartTime(LocalTime.of(9, 0))
//                .operationEndTime(LocalTime.of(22, 0))
//                .externalId("test-external-id")
//                .build());
//
//        // 3. 주차 자리
//        ParkingSpot parkingSpot = parkingSpotRepository.save(ParkingSpot.builder()
//                .parkingLot(parkingLot)
//                .number("A-01")
//                .type(SpotType.SMALL)
//                .build());
//        // 예약 생성 시 주차자리 OCCUPIED로 변경
//        parkingSpot.reserve();
//
//        // 4. PENDING 예약
//        reservationRepository.save(Reservation.builder()
//                .user(user)
//                .parkingLot(parkingLot)
//                .parkingSpot(parkingSpot)
//                .startTime(LocalDateTime.now().minusHours(1))
//                .endTime(LocalDateTime.now().plusHours(2))
//                .status(ReservationStatus.PENDING)
//                .build());
//    }
//}