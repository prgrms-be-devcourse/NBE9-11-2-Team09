package com.example.parking.domain.payment.controller;

import com.example.parking.domain.payment.dto.PaymentReqDto;
import com.example.parking.domain.payment.dto.PaymentRespDto;
import com.example.parking.domain.payment.dto.TossConfirmReqDto;
import com.example.parking.domain.payment.service.PaymentService;
import com.example.parking.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * CUS-05: 결제 시작
     */
    @PostMapping
    public ResponseEntity<PaymentRespDto> startPayment(
            @Valid @RequestBody PaymentReqDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(
                paymentService.startPayment(request, userDetails.getUserId())
        );
    }

    /**
     * CUS-05: 결제 승인 (토스페이먼츠)
     */
    @PostMapping("/{paymentId}/approve")
    public ResponseEntity<PaymentRespDto> approvePayment(
            @PathVariable Long paymentId,
            @RequestBody TossConfirmReqDto tossRequest,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(
                paymentService.approvePayment(paymentId, userDetails.getUserId(), tossRequest)
        );
    }
}