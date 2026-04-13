package com.example.parking.domain.parkingLot.external;

import com.example.parking.domain.parkingLot.external.dto.ParkingApiResDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/*
외부 API를 호출해서 주차장 정보를 조회하는 클라이언트 클래스

- HTTP 요청을 통해 외부 API를 호출하고, 응답 JSON을 ParkingApiResDto로 변환하여 반환
- 비즈니스 로직은 포함하지 않으며, 외부 시스템과의 통신 역할만 담당
*/
@Service
public class ParkingOpenApiClient {

    private final RestClient restClient; // 외부 API 호출을 위한 HTTP 클라이언트 객체
    private final String apiKey;

    // 생성자에서 API 키 주입 및 RestClient 초기화
    public ParkingOpenApiClient(@Value("${openapi.seoul.key}") String apiKey) {
        this.apiKey = apiKey;
        this.restClient = RestClient.builder()
                .baseUrl("http://openapi.seoul.go.kr:8088") // 서울시 공공데이터 API 기본 URL
                .build();
    }

    // 강남구 주차장 데이터를 조회하는 메서드
    public ParkingApiResDto fetchParkingLots() {
        return restClient.get()
                // API 경로 설정 (인증키, 시작/끝 범위, 지역명 포함)
                .uri("/{apiKey}/json/GetParkInfo/1/100/강남", apiKey)
                .retrieve()
                // 응답 JSON을 ParkingApiResDto로 변환
                .body(ParkingApiResDto.class);
    }
}
