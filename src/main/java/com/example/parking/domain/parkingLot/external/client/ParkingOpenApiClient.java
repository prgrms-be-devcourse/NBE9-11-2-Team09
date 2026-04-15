package com.example.parking.domain.parkingLot.external.client;

import com.example.parking.domain.parkingLot.external.dto.ParkingApiDto;
import com.example.parking.domain.parkingLot.external.exception.ExternalApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class ParkingOpenApiClient {

    private static final int START = 1;
    private static final int END = 100;
    private static final String DEFAULT_ADDR = "강남";

    private final SeoulParkingApiClient seoulParkingApiClient;

    @Value("${openapi.seoul.key}")
    private String apiKey;

    // 외부 API 호출
    public ParkingApiDto.Response fetchParkingLots() {
        ParkingApiDto.Response response = seoulParkingApiClient.fetchParkingLots(apiKey, START, END, DEFAULT_ADDR);

        validateResponse(response);

        return response;
    }

    // 외부 API 호출 시 에러 처리
    private void validateResponse(ParkingApiDto.Response response) {
        if (response == null) {
            throw new ExternalApiException("서울시 주차장 API 응답이 null입니다.");
        }

        if (response.parkInfo() == null) {
            throw new ExternalApiException("서울시 주차장 API 응답에 parkInfo가 없습니다.");
        }

        ParkingApiDto.ParkInfo parkInfo = response.parkInfo();

        if (parkInfo.result() == null) {
            throw new ExternalApiException("서울시 주차장 API 응답에 RESULT가 없습니다.");
        }

        if (!"INFO-000".equals(parkInfo.result().code())) {
            throw new ExternalApiException(
                    "서울시 주차장 API 비정상 응답: "
                            + parkInfo.result().code()
                            + " / "
                            + parkInfo.result().message()
            );
        }

        if (parkInfo.items() == null) {
            throw new ExternalApiException("서울시 주차장 API 응답에 row 데이터가 없습니다.");
        }
    }
}
