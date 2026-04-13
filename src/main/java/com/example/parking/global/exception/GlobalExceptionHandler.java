package com.example.parking.global.exception;


//@ControllerAdvice
public class GlobalExceptionHandler {

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