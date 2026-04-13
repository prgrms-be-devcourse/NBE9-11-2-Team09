package com.example.parking.domain.payment.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;

@Getter
public class PaymentReqDto {

    private Long reservationId;

    @Min(value = 1, message = "결제 금액은 0원 이상이어야 합니다.")
    @Max(value = 10000000, message = "결제 금액이 너무 큽니다.")
    private Integer amount;
}