package com.example.parking.domain.payment.entity;

import com.example.parking.domain.reservation.entity.Reservation;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_reservation_id", nullable = false, unique = true)
    private Reservation reservation;

    @Column(name = "payment_amount", nullable = false)
    private Integer amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus status;

    @CreatedDate
    @Column(name = "payment_created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Payment(Reservation reservation, Integer amount) {
        this.reservation = reservation;
        this.amount = amount;
        this.status = PaymentStatus.COMPLETE;
    }


}