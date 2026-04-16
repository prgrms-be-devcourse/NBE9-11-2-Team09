    package com.example.parking.domain.reservation.service;

    import com.example.parking.domain.parkingLot.entity.ParkingLot;
    import com.example.parking.domain.parkingLot.repository.ParkingLotRepository;
    import com.example.parking.domain.parkingspot.entity.ParkingSpot;
    import com.example.parking.domain.parkingspot.entity.SpotStatus;
    import com.example.parking.domain.parkingspot.repository.ParkingSpotRepository;
    import com.example.parking.domain.parkingspot.service.ParkingSpotService;
    import com.example.parking.domain.reservation.dto.ReservationReqDto;
    import com.example.parking.domain.reservation.dto.ReservationResDto;
    import com.example.parking.domain.reservation.entity.Reservation;
    import com.example.parking.domain.reservation.entity.ReservationStatus;
    import com.example.parking.domain.reservation.repository.ReservationRepository;
    import com.example.parking.domain.user.entity.User;
    import com.example.parking.domain.user.repository.UserRepository;
    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.beans.factory.ObjectProvider;
    import org.springframework.scheduling.TaskScheduler;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;

    import java.time.Instant;
    import java.time.LocalDateTime;
    import java.util.List;
    import java.util.stream.Collectors;

    @Slf4j
    @Service
    @RequiredArgsConstructor
    @Transactional(readOnly = true)
    public class ReservationService {

        private final UserRepository userRepository;
        private final ParkingLotRepository parkingLotRepository;
        private final ParkingSpotRepository parkingSpotRepository;
        private final ParkingSpotService parkingSpotService;
        private final ReservationRepository reservationRepository;
        private final TaskScheduler taskScheduler; // 💡 1. TaskScheduler 주입
        private final ObjectProvider<ReservationService> reservationServiceProvider;

        // [CUS-04] 예약 관리 - 내 예약 목록 조회
        public List<ReservationResDto> getMyReservations(Long userId, ReservationStatus status) {
            // Repository의 findAllByUserIdWithDetails 쿼리에 status != 'CANCELLED' 조건이 필요합니다.
            return reservationRepository.findAllByUserIdWithDetails(userId, status).stream()
                    .map(ReservationResDto::from)
                    .collect(Collectors.toList());
        }

        // [CUS-04] 예약 관리 - 내 특정 예약 상세 조회
        public ReservationResDto getReservationDetail(Long reservationId, Long userId) {
            // 이미 취소된 예약은 조회가 되지 않도록 Repository에서 필터링하거나 여기서 상태를 체크합니다.
            Reservation reservation = reservationRepository.findByIdAndUserIdWithDetails(reservationId, userId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 권한이 없는 예약입니다."));

            return ReservationResDto.from(reservation);
        }

        // [CUS-04] 예약 관리 - 예약 취소
        @Transactional
        public void cancelReservation(Long reservationId, Long userId) {
            Reservation reservation = reservationRepository.findByIdAndUserIdWithDetails(reservationId, userId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 이미 취소된 예약입니다."));

            // 1. 이미 취소된 예약인지 확인 (소프트 델리트 중복 방지)
            if (reservation.getStatus() == ReservationStatus.CANCELED ) {
                throw new IllegalStateException("이미 취소 처리된 예약입니다.");
            }

            // 2. 권한 검증
            if (!reservation.getUser().getId().equals(userId)) {
                throw new IllegalArgumentException("해당 예약을 취소할 권한이 없습니다.");
            }

            reservation.cancel();

            // 점유된 자리 해제
            parkingSpotService.release(reservation.getParkingSpot());

            // TODO: 원석님(결제) 환불 로직 연동
            // TODO: 현태님(자리) 상태 AVAILABLE 변경 로직 연동
        }

        // [CUS-03] 예약 생성
        @Transactional
        public ReservationResDto createReservation(Long userId, ReservationReqDto reqDto) {
            // 1. DTO에서 안전하게 파싱된 시간을 가져옵니다.
            LocalDateTime start = reqDto.getParsedStartTime();
            LocalDateTime end = reqDto.getParsedEndTime();

            // 2. 시간 유효성 검증
            if (start.isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("과거 시간으로 예약할 수 없습니다.");
            }
            if (end.isBefore(start)) {
                throw new IllegalArgumentException("종료 시간이 시작 시간보다 앞설 수 없습니다.");
            }

            // 3. 유저와 주차장 정보 조회
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
            ParkingLot parkingLot = parkingLotRepository.findById(reqDto.parkingLotId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주차장입니다."));

            // 4. 주차 자리 조회 (🔥비관적 락 획득)
            ParkingSpot parkingSpot = parkingSpotRepository.findByIdWithLock(reqDto.parkingSpotId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주차 자리입니다."));


            // 💡 수정된 부분 1: 현재 자리가 누군가 결제 중(OCCUPIED)인지 먼저 확인합니다.
            if (parkingSpot.getStatus() == SpotStatus.OCCUPIED) {
                throw new IllegalStateException("현재 다른 사용자가 결제 진행 중인 자리입니다. 잠시 후 다시 시도해주세요.");
            }

            // 4. 선택한 주차장의 ID와 실제 주차 자리가 속한 주차장의 ID가 일치하는지 검증합니다.
            if (!parkingSpot.getParkingLot().getId().equals(reqDto.parkingLotId())) {
                throw new IllegalArgumentException("선택하신 주차장(ID: " + reqDto.parkingLotId() +
                        ")에 해당 주차 자리(ID: " + reqDto.parkingSpotId() + ")가 존재하지 않습니다.");
            }

            // 4-1. 주차 자리와 내 차종이 서로 다를때의 방어로직.
            if (!parkingSpot.getType().name().equals(user.getVehicleType().name())) {
                throw new IllegalStateException("해당 자리는 " + parkingSpot.getType() +
                        " 전용입니다. 고객님의 차종(" + user.getVehicleType() +
                        ")은 이용할 수 없습니다.");
            }

            // 5. 예약 시간 중복 검사
            long overlapCount = reservationRepository.countOverlappingReservations(
                    parkingSpot.getId(), start, end
            );

            if (overlapCount > 0) {
                throw new IllegalStateException("해당 시간에 이미 예약된 자리입니다.");
            }

            // 💡 수정된 부분 2: 검증을 모두 통과했으므로 자리를 5분간 홀딩(OCCUPIED) 상태로 변경합니다.
//            parkingSpot.updateStatus(SpotStatus.OCCUPIED);
            parkingSpotService.reserve(parkingSpot);


            // 6. 예약 엔티티 생성 및 저장
            Reservation newReservation = Reservation.builder()
                    .user(user)
                    .parkingLot(parkingLot)
                    .parkingSpot(parkingSpot)
                    .startTime(start)
                    .endTime(end)
                    .status(ReservationStatus.PENDING)
                    .build();

            Reservation savedReservation = reservationRepository.save(newReservation);
            Long reservationId = savedReservation.getId(); // ID 추출

            // 💡 2. [실시간 취소 예약] 50초 뒤에 아래 cancelIfUnpaid 메서드를 실행합니다.
            taskScheduler.schedule(() -> {
                // 💡 자기 자신의 프록시를 가져와서 호출해야 @Transactional이 정상 작동합니다.
                ReservationService self = reservationServiceProvider.getObject();
                self.cancelIfUnpaid(reservationId);
            }, Instant.now().plusSeconds(300));
            log.info("[예약 생성] 50초 타이머 작동 시작 - 예약 ID: {}", reservationId);

            return ReservationResDto.from(savedReservation);
        }

        @Transactional // 💡 반드시 별도의 트랜잭션으로 실행되어야 함
        public void cancelIfUnpaid(Long reservationId) {
            // 💡 findById 대신 새로 만든 Fetch Join 메서드 사용
            reservationRepository.findByIdWithParkingSpot(reservationId).ifPresent(res -> {
                if (res.getStatus() == ReservationStatus.PENDING) {
                    res.cancel();
                    if (res.getParkingSpot().getStatus() == SpotStatus.OCCUPIED) {
                        res.getParkingSpot().updateStatus(SpotStatus.AVAILABLE);
                    }
                    log.info("[실시간 취소 완료] 예약 ID: {}", reservationId);
                }
            });
        }
    }