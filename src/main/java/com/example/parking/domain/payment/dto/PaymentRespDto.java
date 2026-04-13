package com.example.parking.domain.payment.dto;

import com.example.parking.domain.payment.entity.Payment;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class PaymentRespDto {

    private Long paymentId;
    private String status;
    private String receiptUuid;

    public static PaymentRespDto from(Payment payment) {
        return new PaymentRespDto(
                payment.getId(),
                payment.getStatus().name(),
                UUID.randomUUID().toString()
        );
    }
}