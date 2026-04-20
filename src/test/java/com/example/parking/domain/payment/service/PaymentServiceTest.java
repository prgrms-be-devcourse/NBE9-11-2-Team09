package com.example.parking.domain.payment.service;

import com.example.parking.domain.parkingLot.entity.ParkingLot;
import com.example.parking.domain.parkingspot.entity.ParkingSpot;
import com.example.parking.domain.parkingspot.entity.SpotType;
import com.example.parking.domain.parkingspot.repository.ParkingSpotRepository;
import com.example.parking.domain.payment.dto.PaymentReqDto;
import com.example.parking.domain.payment.dto.PaymentRespDto;
import com.example.parking.domain.payment.dto.TossConfirmReqDto;
import com.example.parking.domain.payment.dto.TossConfirmResDto;
import com.example.parking.domain.payment.entity.Payment;
import com.example.parking.domain.payment.entity.PaymentStatus;
import com.example.parking.domain.payment.infrastructure.TossPaymentClient;
import com.example.parking.domain.payment.repository.PaymentRepository;
import com.example.parking.domain.reservation.entity.Reservation;
import com.example.parking.domain.reservation.entity.ReservationStatus;
import com.example.parking.domain.reservation.repository.ReservationRepository;
import com.example.parking.domain.reservation.service.ReservationService;
import com.example.parking.domain.user.entity.User;
import com.example.parking.domain.user.entity.UserRole;
import com.example.parking.global.sse.SseEmitterManager;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @InjectMocks
    private PaymentService paymentService;

    @Mock private PaymentRepository paymentRepository;
    @Mock private ReservationRepository reservationRepository;
    @Mock private ParkingSpotRepository parkingSpotRepository;
    @Mock private EntityManager entityManager;
    @Mock private ReservationService reservationService;
    @Mock private TossPaymentClient tossPaymentClient;
    @Mock private SseEmitterManager sseEmitterManager;

    private User user;
    private ParkingLot parkingLot;
    private ParkingSpot parkingSpot;
    private Reservation reservation;
    private Payment payment;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .email("test@test.com")
                .password("1234")
                .name("테스트")
                .role(UserRole.USER)
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);

        parkingLot = ParkingLot.builder()
                .name("테스트 주차장")
                .address("서울시")
                .price(1000)
                .totalSpot(10)
                .build();
        ReflectionTestUtils.setField(parkingLot, "id", 1L);

        parkingSpot = ParkingSpot.builder()
                .parkingLot(parkingLot)
                .number("A-01")
                .type(SpotType.SMALL)
                .build();
        ReflectionTestUtils.setField(parkingSpot, "id", 1L);

        reservation = Reservation.builder()
                .user(user)
                .parkingLot(parkingLot)
                .parkingSpot(parkingSpot)
                .startTime(LocalDateTime.now().minusHours(1))
                .endTime(LocalDateTime.now().plusHours(1))
                .status(ReservationStatus.PENDING)
                .build();
        ReflectionTestUtils.setField(reservation, "id", 1L);

        payment = Payment.builder()
                .reservation(reservation)
                .amount(12000)
                .build();
        ReflectionTestUtils.setField(payment, "id", 1L);
    }

    private PaymentReqDto createRequest(Long reservationId, int amount) {
        PaymentReqDto request = new PaymentReqDto();
        ReflectionTestUtils.setField(request, "reservationId", reservationId);
        ReflectionTestUtils.setField(request, "amount", amount);
        return request;
    }

    // ==================== startPayment 테스트 ====================

    @Test
    @DisplayName("결제 시작 - 정상")
    void startPayment_success() {
        // given
        Long userId = 1L;
        Long reservationId = 1L;
        PaymentReqDto request = createRequest(reservationId, 12000);

        ReflectionTestUtils.setField(user, "id", userId);
        given(reservationRepository.findById(reservationId)).willReturn(Optional.of(reservation));
        given(paymentRepository.existsByReservationId(reservationId)).willReturn(false);
        given(parkingSpotRepository.startPayment(anyLong())).willReturn(1);
        given(parkingSpotRepository.findById(anyLong())).willReturn(Optional.of(parkingSpot));
        given(paymentRepository.save(any(Payment.class))).willReturn(payment);

        // when
        PaymentRespDto result = paymentService.startPayment(request, userId);

        // then
        assertThat(result).isNotNull();
        verify(parkingSpotRepository).startPayment(anyLong());
        verify(reservationService).startPaymentProcess(anyLong());
    }

    @Test
    @DisplayName("결제 시작 실패 - 존재하지 않는 예약")
    void startPayment_reservationNotFound() {
        // given
        Long userId = 1L;
        PaymentReqDto request = createRequest(999L, 1000);

        given(reservationRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> paymentService.startPayment(request, userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 예약입니다.");
    }

    @Test
    @DisplayName("결제 시작 실패 - 본인 예약 아님")
    void startPayment_notOwner() {
        // given
        Long userId = 99L;
        Long reservationId = 1L;
        PaymentReqDto request = createRequest(reservationId, 1000);

        ReflectionTestUtils.setField(user, "id", 1L);
        given(reservationRepository.findById(reservationId)).willReturn(Optional.of(reservation));

        // when & then
        assertThatThrownBy(() -> paymentService.startPayment(request, userId))
                .isInstanceOf(SecurityException.class)
                .hasMessage("본인의 예약만 결제할 수 있습니다.");
    }

    @Test
    @DisplayName("결제 시작 실패 - 이미 결제 진행 중인 예약")
    void startPayment_alreadyConfirmed() {
        // given
        Long userId = 1L;
        Long reservationId = 1L;
        PaymentReqDto request = createRequest(reservationId, 1000);

        ReflectionTestUtils.setField(user, "id", userId);
        ReflectionTestUtils.setField(reservation, "status", ReservationStatus.CONFIRMED);
        given(reservationRepository.findById(reservationId)).willReturn(Optional.of(reservation));

        // when & then
        assertThatThrownBy(() -> paymentService.startPayment(request, userId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 결제 진행 중인 예약입니다.");
    }

    @Test
    @DisplayName("결제 시작 실패 - 중복 결제")
    void startPayment_duplicatePayment() {
        // given
        Long userId = 1L;
        Long reservationId = 1L;
        PaymentReqDto request = createRequest(reservationId, 1000);

        ReflectionTestUtils.setField(user, "id", userId);
        given(reservationRepository.findById(reservationId)).willReturn(Optional.of(reservation));
        given(paymentRepository.existsByReservationId(reservationId)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> paymentService.startPayment(request, userId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 결제된 예약입니다.");
    }

    @Test
    @DisplayName("결제 시작 실패 - 금액 불일치")
    void startPayment_amountMismatch() {
        // given
        Long userId = 1L;
        Long reservationId = 1L;
        PaymentReqDto request = createRequest(reservationId, 9999);

        ReflectionTestUtils.setField(user, "id", userId);
        given(reservationRepository.findById(reservationId)).willReturn(Optional.of(reservation));
        given(paymentRepository.existsByReservationId(reservationId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> paymentService.startPayment(request, userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("결제 금액이 올바르지 않습니다.");
    }

    @Test
    @DisplayName("결제 시작 실패 - 주차자리 상태 변경 실패")
    void startPayment_spotStatusChangeFailed() {
        // given
        Long userId = 1L;
        Long reservationId = 1L;
        PaymentReqDto request = createRequest(reservationId, 12000);

        ReflectionTestUtils.setField(user, "id", userId);
        given(reservationRepository.findById(reservationId)).willReturn(Optional.of(reservation));
        given(paymentRepository.existsByReservationId(reservationId)).willReturn(false);
        given(parkingSpotRepository.startPayment(anyLong())).willReturn(0);

        // when & then
        assertThatThrownBy(() -> paymentService.startPayment(request, userId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("결제를 시작할 수 없는 상태입니다.");
    }

    // ==================== approvePayment 테스트 ====================

    @Test
    @DisplayName("결제 승인 - 정상")
    void approvePayment_success() {
        // given
        Long paymentId = 1L;
        Long userId = 1L;
        TossConfirmReqDto tossRequest = new TossConfirmReqDto("paymentKey", "orderId", 1000);
        TossConfirmResDto tossResponse = new TossConfirmResDto();

        ReflectionTestUtils.setField(user, "id", userId);
        ReflectionTestUtils.setField(payment, "status", PaymentStatus.PROCESSING);
        given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));
        given(tossPaymentClient.confirm(any())).willReturn(tossResponse);
        given(parkingSpotRepository.findById(anyLong())).willReturn(Optional.of(parkingSpot));

        // when
        PaymentRespDto result = paymentService.approvePayment(paymentId, userId, tossRequest);

        // then
        assertThat(result).isNotNull();
        verify(tossPaymentClient).confirm(any());
        verify(reservationService).completePayment(anyLong());
    }

    @Test
    @DisplayName("결제 승인 실패 - 존재하지 않는 결제")
    void approvePayment_paymentNotFound() {
        // given
        Long paymentId = 999L;
        Long userId = 1L;
        TossConfirmReqDto tossRequest = new TossConfirmReqDto("paymentKey", "orderId", 1000);

        given(paymentRepository.findById(paymentId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> paymentService.approvePayment(paymentId, userId, tossRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 결제입니다.");
    }

    @Test
    @DisplayName("결제 승인 실패 - 본인 결제 아님")
    void approvePayment_notOwner() {
        // given
        Long paymentId = 1L;
        Long userId = 99L;
        TossConfirmReqDto tossRequest = new TossConfirmReqDto("paymentKey", "orderId", 1000);

        ReflectionTestUtils.setField(user, "id", 1L);
        given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));

        // when & then
        assertThatThrownBy(() -> paymentService.approvePayment(paymentId, userId, tossRequest))
                .isInstanceOf(SecurityException.class)
                .hasMessage("본인의 결제만 승인할 수 있습니다.");
    }

    @Test
    @DisplayName("결제 승인 실패 - PROCESSING 상태 아님")
    void approvePayment_notProcessing() {
        // given
        Long paymentId = 1L;
        Long userId = 1L;
        TossConfirmReqDto tossRequest = new TossConfirmReqDto("paymentKey", "orderId", 1000);

        ReflectionTestUtils.setField(user, "id", userId);
        ReflectionTestUtils.setField(payment, "status", PaymentStatus.COMPLETE);
        given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));

        // when & then
        assertThatThrownBy(() -> paymentService.approvePayment(paymentId, userId, tossRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("결제 진행 중인 상태만 승인할 수 있습니다.");
    }

    // ==================== refundPayment 테스트 ====================

    @Test
    @DisplayName("환불 - 정상")
    void refundPayment_success() {
        // given
        Long paymentId = 1L;

        ReflectionTestUtils.setField(payment, "status", PaymentStatus.COMPLETE);
        given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));
        given(parkingSpotRepository.completePayment(anyLong())).willReturn(1);
        given(parkingSpotRepository.findById(anyLong())).willReturn(Optional.of(parkingSpot));

        // when
        PaymentRespDto result = paymentService.refundPayment(paymentId);

        // then
        assertThat(result).isNotNull();
        verify(parkingSpotRepository).completePayment(anyLong());
    }

    @Test
    @DisplayName("환불 실패 - 존재하지 않는 결제")
    void refundPayment_paymentNotFound() {
        // given
        Long paymentId = 999L;

        given(paymentRepository.findById(paymentId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> paymentService.refundPayment(paymentId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 결제입니다.");
    }

    @Test
    @DisplayName("환불 실패 - 이미 환불된 결제")
    void refundPayment_alreadyRefunded() {
        // given
        Long paymentId = 1L;

        ReflectionTestUtils.setField(payment, "status", PaymentStatus.REFUND);
        given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));

        // when & then
        assertThatThrownBy(() -> paymentService.refundPayment(paymentId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 환불된 결제입니다.");
    }

    @Test
    @DisplayName("환불 실패 - COMPLETE 상태 아님")
    void refundPayment_notComplete() {
        // given
        Long paymentId = 1L;

        ReflectionTestUtils.setField(payment, "status", PaymentStatus.PROCESSING);
        given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));

        // when & then
        assertThatThrownBy(() -> paymentService.refundPayment(paymentId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("환불 가능한 상태가 아닙니다.");
    }


}