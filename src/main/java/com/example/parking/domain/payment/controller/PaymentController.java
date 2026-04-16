package com.example.parking.domain.payment.controller;

import com.example.parking.domain.payment.dto.PaymentReqDto;
import com.example.parking.domain.payment.dto.PaymentRespDto;
import com.example.parking.domain.payment.service.PaymentService;
import com.example.parking.global.response.RsData;
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

    // [CUS-05] 결제 시작 - 예약건에 대해 결제 프로세스(Payer 점유 등) 개시
    @PostMapping
    public ResponseEntity<RsData<PaymentRespDto>> startPayment(
            @Valid @RequestBody PaymentReqDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        PaymentRespDto data = paymentService.startPayment(request, userDetails.getUserId());
        return ResponseEntity.ok(new RsData<>("결제 프로세스가 시작되었습니다.", "200-1", data));
    }

    // [CUS-05] 결제 승인 - 결제 최종 승인 처리 및 예약 확정
    @PostMapping("/{paymentId}/approve")
    public ResponseEntity<RsData<PaymentRespDto>> approvePayment(
            @PathVariable Long paymentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        PaymentRespDto data = paymentService.approvePayment(paymentId, userDetails.getUserId());
        return ResponseEntity.ok(new RsData<>("결제 승인이 완료되었습니다.", "200-2", data));
    }
}