package com.example.parking.domain.parkingLot.external.client;

import com.example.parking.domain.parkingLot.external.dto.ParkingApiDto;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

/*
 * Open API 호출 명세를 정의한 Http Interface
 *
 * - RestClient + Http Interface 기반으로 동작
 * - 실제 구현 없이 인터페이스만으로 HTTP 요청을 정의
 * - ParkingApiClientConfig에서 프록시 객체로 생성됨
 */
@HttpExchange
public interface SeoulParkingApiClient {
    @GetExchange("/{apiKey}/json/GetParkInfo/{start}/{end}/{addr}")
    ParkingApiDto.Response fetchParkingLots(

            @PathVariable("apiKey") String apiKey,   // 인증키 경로 변수 바인딩
            @PathVariable("start") int start,       // 조회 시작 위치
            @PathVariable("end") int end,           // 조회 끝 위치
            @PathVariable("addr") String addr       // 조회 지역 (구 단위)

    );
}
