package com.example.parking.domain.parkingLot.external.config;

import com.example.parking.domain.parkingLot.external.client.SeoulParkingApiClient;
import com.example.parking.domain.parkingLot.external.exception.ExternalApiException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

// RestClient 설정 + Http Interface 구현체 생성
@Configuration
public class ParkingApiClientConfig {

    // [1] 서울시 Open API 호출용 RestClient 등록
    @Bean
    public RestClient seoulParkingRestClient() {
        return RestClient.builder()
                .baseUrl("http://openapi.seoul.go.kr:8088") // 외부 API 기본 URL 설정

                // HTTP 상태 코드가 에러일 경우 공통 처리
                .defaultStatusHandler(
                        HttpStatusCode::isError, // 4xx, 5xx 감지
                        (request, response) -> {
                            // 외부 API 호출 실패 시 커스텀 예외 발생 (Service 이전에 막음)
                            throw new ExternalApiException(
                                    "서울시 주차장 API 호출 실패. status=" + response.getStatusCode()
                            );
                        }
                )

                .build();
    }

    // [2] Http Interface를 기반으로 한 구현체를 동적으로 생성
    @Bean
    public SeoulParkingApiClient seoulParkingApiClient(RestClient seoulParkingRestClient) {

        // 어댑터 생성: Http Interface 정보 → RestClient가 이해할 수 있는 HTTP 요청으로 변환
        RestClientAdapter adapter = RestClientAdapter.create(seoulParkingRestClient);

        // Http Interface의 구현체를 생성하는 팩토리
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(adapter)
                .build();

        // SeoulParkingApiClient 인터페이스의 구현 객체 반환
        return factory.createClient(SeoulParkingApiClient.class);
    }
}