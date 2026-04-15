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
     * CUS-05: 결제 시작
     * - PROCESSING 상태로 저장
     * - 주차자리 PAYING으로 변경 (스케줄러 충돌 방지)
     */
    @Transactional
    public PaymentRespDto startPayment(PaymentReqDto request, Long userId) {

        Reservation reservation = findReservation(request.getReservationId());
        validateOwner(reservation, userId);
        validateReservationStatus(reservation);
        validateDuplicatePayment(request.getReservationId());
        validateAmount(reservation, request.getAmount());

        Payment payment = Payment.builder()
                .reservation(reservation)
                .amount(request.getAmount())
                .build();

        // 결제 시작 시 주차자리 PAYING으로 변경
        reservation.getParkingSpot().updateStatus(SpotStatus.PAYING);

        log.info("결제 시작 - reservationId: {}, userId: {}", request.getReservationId(), userId);
        return PaymentRespDto.from(paymentRepository.save(payment));
    }

    /**
     * CUS-05: 결제 승인
     * - COMPLETE 상태로 변경
     * - 예약 CONFIRMED로 변경
     * - 주차자리 AVAILABLE로 변경
     */
    @Transactional
    public PaymentRespDto approvePayment(Long paymentId, Long userId) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> {
                    log.warn("결제 승인 실패 - 존재하지 않는 결제 paymentId: {}", paymentId);
                    return new IllegalArgumentException("존재하지 않는 결제입니다.");
                });

        if (!payment.getReservation().getUser().getId().equals(userId)) {
            log.warn("결제 승인 실패 - 본인 결제 아님 userId: {}", userId);
            throw new SecurityException("본인의 결제만 승인할 수 있습니다.");
        }

        if (payment.getStatus() != PaymentStatus.PROCESSING) {
            log.warn("결제 승인 실패 - 결제 진행 중 상태가 아님 paymentId: {}", paymentId);
            throw new IllegalStateException("결제 진행 중인 상태만 승인할 수 있습니다.");
        }

        payment.complete();
        payment.getReservation().confirm();
        payment.getReservation().getParkingSpot().updateStatus(SpotStatus.AVAILABLE);

        log.info("결제 승인 완료 - paymentId: {}", paymentId);
        return PaymentRespDto.from(payment);
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
     * - COMPLETE 상태만 환불 가능
     * - 환불 시 주차자리 OCCUPIED로 복원
     */
    @Transactional
    public PaymentRespDto refundPayment(Long paymentId) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> {
                    log.warn("환불 실패 - 존재하지 않는 결제 paymentId: {}", paymentId);
                    return new IllegalArgumentException("존재하지 않는 결제입니다.");
                });

        validateRefundStatus(payment);

        payment.refund();
        payment.getReservation().getParkingSpot().updateStatus(SpotStatus.OCCUPIED);

        log.info("환불 완료 - paymentId: {}", paymentId);
        return PaymentRespDto.from(payment);
    }

    // ==================== private 메서드 ====================

    private Reservation findReservation(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> {
                    log.warn("결제 실패 - 존재하지 않는 예약 reservationId: {}", reservationId);
                    return new IllegalArgumentException("존재하지 않는 예약입니다.");
                });
    }

    private void validateOwner(Reservation reservation, Long userId) {
        if (!reservation.getUser().getId().equals(userId)) {
            log.warn("결제 실패 - 본인 예약 아님 userId: {}, reservationId: {}", userId, reservation.getId());
            throw new SecurityException("본인의 예약만 결제할 수 있습니다.");
        }
    }

    private void validateReservationStatus(Reservation reservation) {
        switch (reservation.getStatus()) {
            case PENDING -> {}
            case CONFIRMED -> {
                log.warn("결제 실패 - 이미 결제된 예약 reservationId: {}", reservation.getId());
                throw new IllegalStateException("이미 결제된 예약입니다.");
            }
            case COMPLETED -> {
                log.warn("결제 실패 - 완료된 예약 reservationId: {}", reservation.getId());
                throw new IllegalStateException("완료된 예약입니다.");
            }
            case CANCELED -> {
                log.warn("결제 실패 - 취소된 예약 reservationId: {}", reservation.getId());
                throw new IllegalStateException("취소된 예약은 결제할 수 없습니다.");
            }
        }
    }

    private void validateDuplicatePayment(Long reservationId) {
        if (paymentRepository.existsByReservationId(reservationId)) {
            log.warn("결제 실패 - 중복 결제 시도 reservationId: {}", reservationId);
            throw new IllegalStateException("이미 결제된 예약입니다.");
        }
    }

    private void validateAmount(Reservation reservation, int amount) {
        int expectedAmount = calculateExpectedAmount(reservation);
        if (amount != expectedAmount) {
            log.warn("결제 실패 - 금액 불일치 reservationId: {}", reservation.getId());
            throw new IllegalArgumentException("결제 금액이 올바르지 않습니다. 예상 금액: " + expectedAmount);
        }
    }

    private void validateRefundStatus(Payment payment) {
        if (payment.getStatus() == PaymentStatus.REFUND) {
            log.warn("환불 실패 - 이미 환불된 결제 paymentId: {}", payment.getId());
            throw new IllegalStateException("이미 환불된 결제입니다.");
        }
        if (payment.getStatus() != PaymentStatus.COMPLETE) {
            log.warn("환불 실패 - 환불 불가 상태 paymentId: {}", payment.getId());
            throw new IllegalStateException("환불 가능한 상태가 아닙니다.");
        }
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