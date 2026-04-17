package com.example.parking.domain.payment.controller;

import com.example.parking.domain.payment.dto.PaymentAdminRespDto;
import com.example.parking.domain.payment.dto.PaymentRespDto;
import com.example.parking.domain.payment.service.PaymentService;
import com.example.parking.global.response.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/payments")
public class PaymentAdminController {
    private final PaymentService paymentService;

    // [ADM-03] 전체 결제 조회 - 관리자 권한으로 시스템 내 전체 결제 내역 확인
    @GetMapping
    public ResponseEntity<RsData<List<PaymentAdminRespDto>>> getAllPayments() {
        List<PaymentAdminRespDto> data = paymentService.getAllPayments();
        return ResponseEntity.ok(new RsData<>("전체 결제 내역 조회가 완료되었습니다.", "200-1", data));
    }

    // [ADM-04] 고객별 결제 조회 - 특정 고객의 모든 결제 이력을 관리자 권한으로 조회
    @GetMapping("/{userId}")
    public ResponseEntity<RsData<List<PaymentAdminRespDto>>> getPaymentsByUser(@PathVariable Long userId) {
        List<PaymentAdminRespDto> data = paymentService.getPaymentsByUser(userId);
        return ResponseEntity.ok(new RsData<>("고객별 결제 내역 조회가 완료되었습니다.", "200-2", data));
    }

    // [ADM-01] 환불 처리 - 관리자 권한으로 특정 결제 건에 대한 강제 환불 수행
    @PatchMapping("/{paymentId}/refund")
    public ResponseEntity<RsData<PaymentRespDto>> refundPayment(@PathVariable Long paymentId) {
        PaymentRespDto data = paymentService.refundPayment(paymentId);
        return ResponseEntity.ok(new RsData<>("환불 처리가 완료되었습니다.", "200-3", data));
    }
}