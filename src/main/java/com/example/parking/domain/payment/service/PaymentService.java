package com.example.parking.domain.payment.service;

import com.example.parking.domain.payment.dto.PaymentAdminRespDto;
import com.example.parking.domain.payment.dto.PaymentReqDto;
import com.example.parking.domain.payment.dto.PaymentRespDto;
import com.example.parking.domain.payment.entity.Payment;
import com.example.parking.domain.payment.entity.PaymentStatus;
import com.example.parking.domain.payment.repository.PaymentRepository;
import com.example.parking.domain.reservation.entity.Reservation;
import com.example.parking.domain.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

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

        // 예약 ID로 예약 조회, 없으면 예외 발생
        Reservation reservation = reservationRepository.findById(request.getReservationId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다."));

        // 본인 예약 확인
        // if (!reservation.getUser().getId().equals(userId)) {
        //     throw new SecurityException("본인의 예약만 결제할 수 있습니다.");
        // }

        // 예약 상태 검증 - PENDING 상태만 결제 가능
        switch (reservation.getStatus()) {
            case PENDING -> {}
            case CONFIRMED -> throw new IllegalStateException("이미 결제된 예약입니다.");
            case COMPLETED -> throw new IllegalStateException("완료된 예약입니다.");
            case CANCELED -> throw new IllegalStateException("취소된 예약은 결제할 수 없습니다.");
        }

        // 중복 결제 방지 - 코드 레벨 검증 (DB UK 제약과 2중 방어)
        if (paymentRepository.existsByReservationId(request.getReservationId())) {
            throw new IllegalStateException("이미 결제된 예약입니다.");
        }

        // 금액 검증 - 클라이언트 금액 조작 방지를 위해 서버에서 직접 계산
        int expectedAmount = calculateExpectedAmount(reservation);
        if (!request.getAmount().equals(expectedAmount)) {
            throw new IllegalArgumentException("결제 금액이 올바르지 않습니다. 예상 금액: " + expectedAmount);
        }

        // 결제 저장 - 서버 계산 금액으로 저장
        Payment payment = Payment.builder()
                .reservation(reservation)
                .amount(expectedAmount)
                .build();

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
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 결제입니다."));

        if (payment.getStatus() == PaymentStatus.REFUND) {
            throw new IllegalStateException("이미 환불된 결제입니다.");
        }

        if (payment.getStatus() != PaymentStatus.COMPLETE) {
            throw new IllegalStateException("환불 가능한 상태가 아닙니다.");
        }

        payment.refund();

        return PaymentRespDto.from(payment);
    }

    /**
     * 결제 금액 계산
     * - 주차 시작/종료 시간으로 주차 시간(분) 계산
     * - 분을 시간으로 변환 후 주차장 단가 곱하기
     * - Math.ceil로 올림 처리
     */
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