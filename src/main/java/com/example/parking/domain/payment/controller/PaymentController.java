package com.example.parking.domain.payment.controller;

import com.example.parking.domain.payment.dto.PaymentAdminRespDto;
import com.example.parking.domain.payment.dto.PaymentReqDto;
import com.example.parking.domain.payment.dto.PaymentRespDto;
import com.example.parking.domain.payment.service.PaymentService;
import com.example.parking.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * CUS-05: 결제
     * 고객이 예약한 주차 공간에 대해 결제를 진행한다.
     */
    @PostMapping("/payments")
    public ResponseEntity<PaymentRespDto> processPayment(
            @Valid @RequestBody PaymentReqDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(
                paymentService.processPayment(request, userDetails.getUserId())
        );
    }

    /**
     * ADM-03: 전체 고객별 예약 및 결제 조회
     * 관리자가 전체 결제 내역을 확인한다.
     */
    @GetMapping("/admin/payments")
    public ResponseEntity<List<PaymentAdminRespDto>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    /**
     * ADM-04: 특정 고객별 예약 및 결제 조회
     * 관리자가 특정 고객의 결제 내역을 확인한다.
     */
    @GetMapping("/admin/payments/{userId}")
    public ResponseEntity<List<PaymentAdminRespDto>> getPaymentsByUser(
            @PathVariable Long userId) {
        return ResponseEntity.ok(paymentService.getPaymentsByUser(userId));
    }

    /**
     * ADM-01: 고객 예약 삭제 - 환불 처리
     * 관리자가 특정 결제를 환불 처리한다.
     */
    @PatchMapping("/admin/payments/{paymentId}/refund")
    public ResponseEntity<PaymentRespDto> refundPayment(
            @PathVariable Long paymentId) {
        return ResponseEntity.ok(paymentService.refundPayment(paymentId));
    }
}