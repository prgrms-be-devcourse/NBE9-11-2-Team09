package com.example.parking.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;

//@RestControllerAdvice
public class GlobalExceptionHandler {

//  // 존재하지 않는 예약, 금액 불일치
//  @ExceptionHandler(IllegalArgumentException.class)
//  public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
//    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//        .body(new ErrorResponse("BAD_REQUEST", e.getMessage()));
//  }
//
//  // 중복 결제, 예약 상태 오류
//  @ExceptionHandler(IllegalStateException.class)
//  public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException e) {
//    return ResponseEntity.status(HttpStatus.CONFLICT)
//        .body(new ErrorResponse("CONFLICT", e.getMessage()));
//  }
//
// 본인 예약 아닐 때
// @ExceptionHandler(SecurityException.class)
// public ResponseEntity<ErrorResponse> handleSecurity(SecurityException e) {
//    return ResponseEntity.status(HttpStatus.FORBIDDEN)
//            .body(new ErrorResponse("FORBIDDEN", e.getMessage()));
// }
//
//
//  @ExceptionHandler(NoSuchElementException.class)
//  @ResponseBody
//  public RsData<Void> handleException(){
//    return new RsData<Void>(
//        "존재하지 않는 데이터입니다.",
//        "404-1"
//    );
//  }
//
//  @ExceptionHandler(MethodArgumentNotValidException.class)
//  @ResponseBody
//  public RsData<Void> handleException(MethodArgumentNotValidException e) {
//    String message = e.getBindingResult()
//        .getAllErrors()
//        .stream()
//        .filter(error -> error instanceof FieldError)
//        .map(error -> (FieldError) error)
//        .map(error -> error.getField() + "-" + error.getCode() + "-" + error.getDefaultMessage())
//        .sorted(Comparator.comparing(String::toString))
//        .collect(Collectors.joining("\n"));
//
//    return new RsData<Void>(
//        message,
//        "400-1"
//    );
//  }
//
//  @ExceptionHandler(HttpMessageNotReadableException.class)
//  @ResponseBody
//  public RsData<Void> handleException(HttpMessageNotReadableException e) {
//    return new RsData<Void>(
//        "잘못된 형식의 요청 데이터입니다.",
//        "400-2"
//    );
//  }
//
//  @ExceptionHandler(ServiceException.class)
//  @ResponseBody
//  public RsData<Void> handleException(ServiceException e) {
//    return e.getRsData();
//  }
//
//  @ExceptionHandler(HandlerMethodValidationException.class)
//  @ResponseBody
//  public RsData<Void> handleException(HandlerMethodValidationException e) {
//    return new RsData<Void>(
//        "잘못된 파라미터 요청입니다.",
//        "400-4"
//    );
//  }
}