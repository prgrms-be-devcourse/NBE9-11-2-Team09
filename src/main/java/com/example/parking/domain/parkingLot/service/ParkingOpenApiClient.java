package com.example.parking.domain.parkingLot.service;

import com.example.parking.domain.parkingLot.dto.ParkingApiResDto;
import org.springframework.web.client.RestClient;

public class ParkingOpenApiClient {

    private final RestClient restClient;

    public ParkingOpenApiClient() {
        this.restClient = RestClient.builder()
                .baseUrl("http://openapi.seoul.go.kr:8088")
                .build();
    }

    public ParkingApiResDto fetchParkingLots(String apiKey, int start, int end, String addr) {
        return restClient.get()
                .uri("/{apiKey}/json/GetParkInfo/{start}/{end}/{addr}", apiKey, start, end, addr)
                .retrieve()
                .body(ParkingApiResDto.class);
    }
}
