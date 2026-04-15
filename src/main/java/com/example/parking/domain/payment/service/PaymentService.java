package com.example.parking.domain.payment.service;

import com.example.parking.domain.parkingspot.entity.SpotStatus;
import com.example.parking.domain.payment.dto.PaymentAdminRespDto;
import com.example.parking.domain.payment.dto.PaymentReqDto;
import com.example.parking.domain.payment.dto.PaymentRespDto;
import com.example.parking.domain.payment.entity.Payment;
import com.example.parking.domain.payment.entity.PaymentStatus;
import com.example.parking.domain.payment.repository.PaymentRepository;
import com.example.parking.domain.reservation.entity.Reservation;
import com.example.parking.domain.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;

    /**
     * CUS-05: 결제 진행
     * - 예약 존재 여부, 본인 확인, 상태 검증, 중복 결제 방지, 금액 검증 순서로 처리
     * - 모든 검증 통과 시 결제 저장
     */
    @Transactional
    public PaymentRespDto processPayment(PaymentReqDto request, Long userId) {

        Reservation reservation = reservationRepository.findById(request.getReservationId())
                .orElseThrow(() -> {
                    log.warn("결제 실패 - 존재하지 않는 예약 reservationId: {}", request.getReservationId());
                    return new IllegalArgumentException("존재하지 않는 예약입니다.");
                });

        if (!reservation.getUser().getId().equals(userId)) {
            log.warn("결제 실패 - 본인 예약 아님 userId: {}, reservationId: {}", userId, request.getReservationId());
            throw new SecurityException("본인의 예약만 결제할 수 있습니다.");
        }

        switch (reservation.getStatus()) {
            case PENDING -> {}
            case CONFIRMED -> {
                log.warn("결제 실패 - 이미 결제된 예약 reservationId: {}", request.getReservationId());
                throw new IllegalStateException("이미 결제된 예약입니다.");
            }
            case COMPLETED -> {
                log.warn("결제 실패 - 완료된 예약 reservationId: {}", request.getReservationId());
                throw new IllegalStateException("완료된 예약입니다.");
            }
            case CANCELED -> {
                log.warn("결제 실패 - 취소된 예약 reservationId: {}", request.getReservationId());
                throw new IllegalStateException("취소된 예약은 결제할 수 없습니다.");
            }
        }

        if (paymentRepository.existsByReservationId(request.getReservationId())) {
            log.warn("결제 실패 - 중복 결제 시도 reservationId: {}", request.getReservationId());
            throw new IllegalStateException("이미 결제된 예약입니다.");
        }

        int expectedAmount = calculateExpectedAmount(reservation);
        if (!request.getAmount().equals(expectedAmount)) {
            log.warn("결제 실패 - 금액 불일치 reservationId: {}", request.getReservationId());
            throw new IllegalArgumentException("결제 금액이 올바르지 않습니다. 예상 금액: " + expectedAmount);
        }

        Payment payment = Payment.builder()
                .reservation(reservation)
                .amount(expectedAmount)
                .build();

        // 결제가 성공했으므로, 5분간 홀딩(OCCUPIED)했던 자리를 다시 이용 가능(AVAILABLE)으로 풉니다.
        // 그래야 다른 사람이 내가 예약한 시간 외의 '다른 시간대'를 예약할 수 있습니다.
        reservation.getParkingSpot().updateStatus(SpotStatus.AVAILABLE);

        // 예약 상태를 PENDING -> CONFIRMED(확정)로 변경합니다.
        reservation.confirm();

        return PaymentRespDto.from(paymentRepository.save(payment));
    }

    /**
     * ADM-03: 전체 결제 조회
     * - Fetch Join으로 Payment + Reservation + User 한번에 조회
     * - readOnly 트랜잭션으로 조회 성능 최적화
     */
    @Transactional(readOnly = true)
    public List<PaymentAdminRespDto> getAllPayments() {
        return paymentRepository.findAllWithReservationAndUser()
                .stream()
                .map(PaymentAdminRespDto::from)
                .collect(Collectors.toList());
    }

    /**
     * ADM-04: 고객별 결제 조회
     * - userId 기반으로 해당 고객 결제만 조회
     * - Fetch Join으로 N+1 문제 방지
     * - readOnly 트랜잭션으로 조회 성능 최적화
     */
    @Transactional(readOnly = true)
    public List<PaymentAdminRespDto> getPaymentsByUser(Long userId) {
        return paymentRepository.findAllByUserIdWithReservationAndUser(userId)
                .stream()
                .map(PaymentAdminRespDto::from)
                .collect(Collectors.toList());
    }

    /**
     * ADM-01: 환불 처리
     * - 결제 존재 여부 확인
     * - COMPLETE 상태만 환불 가능
     * - 이미 환불된 결제 중복 환불 방지
     */
    @Transactional
    public PaymentRespDto refundPayment(Long paymentId) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> {
                    log.warn("환불 실패 - 존재하지 않는 결제 paymentId: {}", paymentId);
                    return new IllegalArgumentException("존재하지 않는 결제입니다.");
                });

        if (payment.getStatus() == PaymentStatus.REFUND) {
            log.warn("환불 실패 - 이미 환불된 결제 paymentId: {}", paymentId);
            throw new IllegalStateException("이미 환불된 결제입니다.");
        }

        if (payment.getStatus() != PaymentStatus.COMPLETE) {
            log.warn("환불 실패 - 환불 불가 상태 paymentId: {}", paymentId);
            throw new IllegalStateException("환불 가능한 상태가 아닙니다.");
        }

        payment.refund();
        return PaymentRespDto.from(payment);
    }

    private int calculateExpectedAmount(Reservation reservation) {
        long minutes = ChronoUnit.MINUTES.between(
                reservation.getStartTime(),
                reservation.getEndTime()
        );
        double hours = minutes / 60.0;
        int price = reservation.getParkingLot().getPrice();
        return (int) Math.ceil(hours * price);
    }
}