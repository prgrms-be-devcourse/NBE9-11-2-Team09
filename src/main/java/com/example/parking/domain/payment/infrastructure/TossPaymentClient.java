package com.example.parking.domain.payment.infrastructure;

import com.example.parking.domain.payment.config.TossPaymentConfig;
import com.example.parking.domain.payment.dto.TossConfirmReqDto;
import com.example.parking.domain.payment.dto.TossConfirmResDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Base64;

@Slf4j
@Component
@RequiredArgsConstructor
public class TossPaymentClient {

    private final TossPaymentConfig tossPaymentConfig;

    public TossConfirmResDto confirm(TossConfirmReqDto request) {
        String encodedSecretKey = Base64.getEncoder()
                .encodeToString((tossPaymentConfig.getSecretKey() + ":").getBytes());

        RestClient restClient = RestClient.create();

        return restClient.post()
                .uri(tossPaymentConfig.getUrl())
                .header(HttpHeaders.AUTHORIZATION, "Basic " + encodedSecretKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(TossConfirmResDto.class);
    }
}