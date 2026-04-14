package com.example.parking.domain.parkingLot.external;

import com.example.parking.domain.parkingLot.external.dto.ParkingApiDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class ParkingOpenApiClient {

    private final RestClient restClient; // 외부 API 호출을 위한 HTTP 클라이언트 객체
    private final String apiKey;

    // [CUS-01] 생성자에서 API 키 주입 및 RestClient 초기화
    public ParkingOpenApiClient(@Value("${openapi.seoul.key}") String apiKey) {
        this.apiKey = apiKey;
        this.restClient = RestClient.builder()
                .baseUrl("http://openapi.seoul.go.kr:8088") // 서울시 공공데이터 API 기본 URL
                .build();
    }

    // [CUS-01] 외부 API를 호출하여 강남구 주차장 데이터를 조회
    public ParkingApiDto.Response fetchParkingLots() {
        return restClient.get()
                // API 경로 설정 (인증키, 시작/끝 범위, 지역명 포함)
                .uri("/{apiKey}/json/GetParkInfo/1/100/강남", apiKey)
                .retrieve()
                // 응답 JSON을 ParkingApiDto로 변환
                .body(ParkingApiDto.Response.class);
    }
}
