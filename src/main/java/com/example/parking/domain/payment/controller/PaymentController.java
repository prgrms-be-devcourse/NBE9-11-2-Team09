package com.example.parking.domain.payment.controller;

import com.example.parking.domain.payment.dto.PaymentReqDto;
import com.example.parking.domain.payment.dto.PaymentRespDto;
import com.example.parking.domain.payment.dto.TossConfirmReqDto;
import com.example.parking.domain.payment.service.PaymentService;
import com.example.parking.global.response.RsData;
import com.example.parking.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
@Tag(name = "결제", description = "결제 관련 API")
public class PaymentController {

    private final PaymentService paymentService;

    // [CUS-05] 결제 시작 - 예약건에 대해 결제 프로세스(Payer 점유 등) 개시
    @Operation(summary = "결제 시작", description = "예약건에 대해 결제 프로세스를 시작합니다. 자리 상태가 PAYING으로 변경됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "결제 시작 완료"),
            @ApiResponse(responseCode = "400", description = "존재하지 않는 예약 또는 금액 불일치"),
            @ApiResponse(responseCode = "403", description = "본인 예약 아님"),
            @ApiResponse(responseCode = "409", description = "이미 결제 진행 중이거나 취소된 예약")
    })
    @PostMapping
    public ResponseEntity<RsData<PaymentRespDto>> startPayment(
            @Valid @RequestBody PaymentReqDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        PaymentRespDto data = paymentService.startPayment(request, userDetails.getUserId());
        return ResponseEntity.ok(new RsData<>("결제 프로세스가 시작되었습니다.", "200-1", data));
    }

    // [CUS-05] 결제 승인 - 결제 최종 승인 처리 및 예약 확정
    @Operation(summary = "결제 승인", description = "토스페이먼츠 결제 승인을 처리하고 예약을 확정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "결제 승인 완료"),
            @ApiResponse(responseCode = "400", description = "존재하지 않는 결제"),
            @ApiResponse(responseCode = "403", description = "본인 결제 아님"),
            @ApiResponse(responseCode = "409", description = "결제 진행 중 상태가 아님"),
            @ApiResponse(responseCode = "500", description = "토스페이먼츠 승인 실패")
    })
    @PostMapping("/{paymentId}/approve")
    public ResponseEntity<RsData<PaymentRespDto>> approvePayment(
            @PathVariable Long paymentId,
            @RequestBody TossConfirmReqDto tossRequest,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        PaymentRespDto data = paymentService.approvePayment(paymentId, userDetails.getUserId(), tossRequest);
        return ResponseEntity.ok(new RsData<>("결제 승인이 완료되었습니다.", "200-2", data));
    }
}