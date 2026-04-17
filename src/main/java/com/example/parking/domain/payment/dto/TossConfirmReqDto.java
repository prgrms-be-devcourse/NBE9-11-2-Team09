package com.example.parking.domain.payment.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TossConfirmReqDto {

    private String paymentKey;  // 토스에서 발급한 결제 키
    private String orderId;     // 주문 ID (우리 시스템의 receiptUuid)
    private Integer amount;     // 결제 금액

    public TossConfirmReqDto(String paymentKey, String orderId, Integer amount) {
        this.paymentKey = paymentKey;
        this.orderId = orderId;
        this.amount = amount;
    }
}