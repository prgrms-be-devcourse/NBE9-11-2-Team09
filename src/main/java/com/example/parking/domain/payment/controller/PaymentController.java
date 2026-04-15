package com.example.parking.domain.payment.controller;

import com.example.parking.domain.payment.dto.PaymentReqDto;
import com.example.parking.domain.payment.dto.PaymentRespDto;
import com.example.parking.domain.payment.service.PaymentService;
import com.example.parking.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * CUS-05: 결제
     */
    @PostMapping
    public ResponseEntity<PaymentRespDto> startPayment(
            @Valid @RequestBody PaymentReqDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(
                paymentService.startPayment(request, userDetails.getUserId())
        );
    }
}