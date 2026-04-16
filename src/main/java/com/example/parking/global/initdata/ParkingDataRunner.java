package com.example.parking.global.initdata;

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

