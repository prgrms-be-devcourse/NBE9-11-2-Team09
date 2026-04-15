package com.example.parking.domain.parkingLot.external.exception;

// [CUS-01] 외부 API 전용 예외 클래스
public class ExternalApiException extends RuntimeException {

    public ExternalApiException(String message) {
        super(message);
    }

    public ExternalApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
