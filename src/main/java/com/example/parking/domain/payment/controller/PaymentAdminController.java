package com.example.parking.domain.payment.controller;
import com.example.parking.domain.payment.dto.PaymentAdminRespDto;
import com.example.parking.domain.payment.dto.PaymentRespDto;
import com.example.parking.domain.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/payments")
public class PaymentAdminController {

    private final PaymentService paymentService;

    /**
     * ADM-03: 전체 결제 조회
     */
    @GetMapping
    public ResponseEntity<List<PaymentAdminRespDto>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    /**
     * ADM-04: 특정 고객별 결제 조회
     */
    @GetMapping("/{userId}")
    public ResponseEntity<List<PaymentAdminRespDto>> getPaymentsByUser(
            @PathVariable Long userId) {
        return ResponseEntity.ok(paymentService.getPaymentsByUser(userId));
    }

    /**
     * ADM-01: 환불 처리
     */
    @PatchMapping("/{paymentId}/refund")
    public ResponseEntity<PaymentRespDto> refundPayment(
            @PathVariable Long paymentId) {
        return ResponseEntity.ok(paymentService.refundPayment(paymentId));
    }
}