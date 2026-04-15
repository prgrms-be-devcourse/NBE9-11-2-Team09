    package com.example.parking.domain.reservation.service;

    import com.example.parking.domain.parkingLot.entity.ParkingLot;
    import com.example.parking.domain.parkingLot.repository.ParkingLotRepository;
    import com.example.parking.domain.parkingspot.entity.ParkingSpot;
    import com.example.parking.domain.parkingspot.repository.ParkingSpotRepository;
    import com.example.parking.domain.reservation.dto.ReservationReqDto;
    import com.example.parking.domain.reservation.dto.ReservationResDto;
    import com.example.parking.domain.reservation.entity.Reservation;
    import com.example.parking.domain.reservation.entity.ReservationStatus;
    import com.example.parking.domain.reservation.repository.ReservationRepository;
    import com.example.parking.domain.user.entity.User;
    import com.example.parking.domain.user.repository.UserRepository;
    import lombok.RequiredArgsConstructor;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;

    import java.time.LocalDateTime;
    import java.util.List;
    import java.util.stream.Collectors;

    @Service
    @RequiredArgsConstructor
    @Transactional(readOnly = true)
    public class ReservationService {

        private final UserRepository userRepository;
        private final ParkingLotRepository parkingLotRepository;
        private final ParkingSpotRepository parkingSpotRepository;
        private final ReservationRepository reservationRepository;

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

            // TODO: 원석님(결제) 환불 로직 연동
            // TODO: 현태님(자리) 상태 AVAILABLE 변경 로직 연동
        }

        // [CUS-03] 예약 생성
        @Transactional
        public ReservationResDto createReservation(Long userId, ReservationReqDto reqDto) {
            // 💡 1. DTO에서 안전하게 파싱된 시간을 가져옵니다.
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

            // 선택한 주차장의 ID와 실제 주차 자리가 속한 주차장의 ID가 일치하는지 검증합니다.
            if (!parkingSpot.getParkingLot().getId().equals(reqDto.parkingLotId())) {
                throw new IllegalArgumentException("선택하신 주차장(ID: " + reqDto.parkingLotId() +
                        ")에 해당 주차 자리(ID: " + reqDto.parkingSpotId() + ")가 존재하지 않습니다.");
            }

            // 5. 예약 시간 중복 검사
            // 핵심: 취소된 예약(CANCELLED)은 중복 카운트에서 제외하도록 Repository 쿼리가 수정되어야 합니다.
            long overlapCount = reservationRepository.countOverlappingReservations(
                    parkingSpot.getId(), start, end // 👈 여기서 파싱된 변수를 사용합니다.
            );

            if (overlapCount > 0) {
                throw new IllegalStateException("해당 시간에 이미 예약된 자리입니다.");
            }

            // 6. 검증을 모두 통과했으므로 예약 엔티티 생성 및 저장
            Reservation newReservation = Reservation.builder()
                    .user(user)
                    .parkingLot(parkingLot)
                    .parkingSpot(parkingSpot)
                    .startTime(start) // 👈 파싱된 변수 적용
                    .endTime(end)     // 👈 파싱된 변수 적용
                    .status(ReservationStatus.PENDING) // 초기 상태 명시
                    .build();

            Reservation savedReservation = reservationRepository.save(newReservation);

            // 7. DTO로 변환하여 반환
            return ReservationResDto.from(savedReservation);
        }
    }