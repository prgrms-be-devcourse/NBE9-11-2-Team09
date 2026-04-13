package com.example.parking.domain.payment.dto;

import com.example.parking.domain.payment.entity.Payment;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PaymentAdminRespDto {

    private Long paymentId;
    private Long reservationId;
    private Long userId;
    private String userName;
    private Integer amount;
    private String status;
    private LocalDateTime createdAt;

    public static PaymentAdminRespDto from(Payment payment) {
        return new PaymentAdminRespDto(
                payment.getId(),
                payment.getReservation().getId(),
                payment.getReservation().getUser().getId(),
                payment.getReservation().getUser().getName(),
                payment.getAmount(),
                payment.getStatus().name(),
                payment.getCreatedAt()
        );
    }
}