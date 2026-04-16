package com.example.parking.global.exception;

import com.example.parking.global.response.RsData;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<RsData<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        RsData<Void> rsData = new RsData<>(e.getMessage(), "400-1");
        return ResponseEntity.status(rsData.getStatusCode()).body(rsData);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<RsData<Void>> handleIllegalStateException(IllegalStateException e) {
        RsData<Void> rsData = new RsData<>(e.getMessage(), "409-1");
        return ResponseEntity.status(rsData.getStatusCode()).body(rsData);
    }

}