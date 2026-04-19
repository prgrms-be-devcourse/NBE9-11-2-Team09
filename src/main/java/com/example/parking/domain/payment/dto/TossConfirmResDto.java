package com.example.parking.domain.payment.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TossConfirmResDto {

    private String paymentKey;    // 토스 결제 키
    private String orderId;       // 주문 ID
    private String orderName;     // 주문명
    private Integer totalAmount;  // 결제 금액
    private String status;        // 결제 상태 (DONE, CANCELED 등)
    private String requestedAt;   // 결제 요청 시간
    private String approvedAt;    // 결제 승인 시간
    private String method;        // 결제 수단 (카드, 가상계좌 등)
}