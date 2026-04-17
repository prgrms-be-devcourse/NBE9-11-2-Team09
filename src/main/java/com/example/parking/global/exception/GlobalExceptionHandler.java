package com.example.parking.global.exception;

import com.example.parking.domain.parkingLot.external.exception.ExternalApiException;
import com.example.parking.global.response.RsData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // [CUS-04, CUS-08, ADM-01] 잘못된 인자 전달 시 처리 (존재하지 않는 ID, 비밀번호 불일치 등)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<RsData<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("IllegalArgumentException: {}", e.getMessage());
        RsData<Void> rsData = new RsData<>(e.getMessage(), "400-1");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(rsData);
    }

    // [CUS-03, CUS-05, ADM-01] 로직 수행 불가 상태 처리 (중복 예약, 결제 상태 부적절 등)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<RsData<Void>> handleIllegalStateException(IllegalStateException e) {
        log.warn("IllegalStateException: {}", e.getMessage());
        RsData<Void> rsData = new RsData<>(e.getMessage(), "409-1");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(rsData);
    }

    // [CUS-05] 권한 부족 처리 (본인 예약/결제 미해당 시)
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<RsData<Void>> handleSecurityException(SecurityException e) {
        log.warn("SecurityException: {}", e.getMessage());
        RsData<Void> rsData = new RsData<>(e.getMessage(), "403-1");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(rsData);
    }

    // [CUS-01] 외부 API 호출 실패 처리
    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<RsData<Void>> handleExternalApiException(ExternalApiException e) {
        log.error("ExternalApiException: {}", e.getMessage());
        RsData<Void> rsData = new RsData<>(e.getMessage(), "502-1");
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(rsData);
    }

    // 그 외 예상치 못한 모든 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<RsData<Void>> handleAllException(Exception e) {
        log.error("Unhandled Exception: ", e);
        RsData<Void> rsData = new RsData<>("서버 내부 오류가 발생했습니다.", "500-1");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(rsData);
    }

}