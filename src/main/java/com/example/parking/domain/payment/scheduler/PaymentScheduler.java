package com.example.parking.domain.payment.scheduler;

import com.example.parking.domain.parkingspot.repository.ParkingSpotRepository;
import com.example.parking.domain.payment.entity.Payment;
import com.example.parking.domain.payment.entity.PaymentStatus;
import com.example.parking.domain.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentScheduler {

    private final PaymentRepository paymentRepository;
    private final ParkingSpotRepository parkingSpotRepository;

    @Scheduled(fixedDelay = 60000) // 1분마다 실행
    @Transactional
    public void cancelExpiredPayments() {
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(5);

        List<Payment> expiredPayments = paymentRepository
                .findByStatusAndCreatedAtBefore(PaymentStatus.PROCESSING, deadline);

        for (Payment payment : expiredPayments) {
            payment.fail();
            parkingSpotRepository.failPayment(
                    payment.getReservation().getParkingSpot().getId());
            log.info("결제 만료 처리 - paymentId: {}", payment.getId());
        }
    }
}