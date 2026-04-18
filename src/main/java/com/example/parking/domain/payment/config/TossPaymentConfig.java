package com.example.parking.domain.payment.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class TossPaymentConfig {

    @Value("${toss.payment.secret-key}")
    private String secretKey;

    @Value("${toss.payment.url}")
    private String url;
}