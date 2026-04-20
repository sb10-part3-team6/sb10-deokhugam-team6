package com.codeit.mission.deokhugam.error;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/*
    애플리케이션 전역에서 발생하는 예외를 한곳에서 관리하고 공통 응답 형식을 반환하는 클래스
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  // Custom Exception: 비지니스 로직 수준에서 발생하는 커스텀 예외 처리
  @ExceptionHandler(DeokhugamException.class)
  public ResponseEntity<ErrorResponse> handleDeokhugamException(DeokhugamException e) {
    ErrorCode errorCode = e.getErrorCode();

    ErrorResponse error = ErrorResponse.builder()
        .timestamp(e.getTimestamp())                                // 에러 발생 시각
        .code(errorCode.name())                                     // 에러 발생 코드 ex) U001
        .message(e.getMessage())                                    // 에러 메시지 ex) "user with id not found"
        .details(e.getDetails())                                    // 에러와 관련된 추가 정보 ex) userid
        .exceptionType(e.getClass().getSimpleName())                // 발생한 예외 클래스 이름
        .status(errorCode.getHttpStatus().value())                  // 발생한 에러의 HTTP Status
        .build();

    // 발생한 에러의 우선 순위 별로 로그 기록
    if (error.status()
        >= 500) {                                                                     // 실제 서버 에러 (error)
      log.error("[CUSTOM_EXCEPTION] ERROR_CODE={}, Message={}, details={}",
          error.code(),
          error.message(),
          error.details()
      );
    } else {                                                                          // 사용자 실수로 인한 에러 (warn)
      log.warn("[CUSTOM_EXCEPTION] ERROR_CODE={}, Message={}, details={}",
          error.code(),
          error.message(),
          error.details());
    }
    return ResponseEntity.status(error.status()).body(error);
  }

  // DTO 검증 오류 (@Valid)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
          MethodArgumentNotValidException e) {
    // 첫번째 에러의 메시지를 전체 응답의 대표 메시지로 설정
    String firstErrorMessage = e.getBindingResult().getAllErrors().stream()
        .findFirst()
        .map(DefaultMessageSourceResolvable::getDefaultMessage)
        .orElse(ErrorCode.INVALID_INPUT_VALUE.getMessage());

    // 핃르 에러(Filed Error)와 관련된 추가 정보 목록
    Map<String, Object> details = new HashMap<>();
    e.getBindingResult().getFieldErrors().forEach(
        fieldError -> {
          details.put(
              fieldError.getField(),              // 에러가 발생한 변수 이름
              fieldError.getDefaultMessage()      // 해당 변수에 설정된 에러 메시지
          );
        }
    );

    ErrorResponse error = ErrorResponse.builder()
        .timestamp(Instant.now())
        .code(ErrorCode.INVALID_INPUT_VALUE.name())
        .message(firstErrorMessage)
        .details(details)
        .exceptionType(e.getClass().getSimpleName())
        .status(HttpStatus.BAD_REQUEST.value())
        .build();

    log.warn("[VALIDATION_EXCEPTION] ERROR_CODE={}, Message={}, Invalid Input={}",
        error.code(),
        error.message(),
        error.details()
    );
    return ResponseEntity.status(error.status()).body(error);
  }

  // 필수 파라미터 누락 오류 (@RequestParam)
  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(
      MissingServletRequestParameterException e) {
    ErrorCode errorCode = ErrorCode.MISSING_REQUEST_PARAMETER;

    ErrorResponse error = ErrorResponse.builder()
        .timestamp(Instant.now())
        .code(errorCode.name())
        .message(errorCode.getMessage())
        .exceptionType(e.getClass().getSimpleName())
        .status(errorCode.getHttpStatus().value())
        .build();

    log.warn("[MISSING_PARAM_EXCEPTION] ERROR_CODE={}, Message={}",
        error.code(),
        error.message()
    );
    return ResponseEntity.status(error.status()).body(error);
  }

  // 파라미터 타입 불일치 (@PathVariable)
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
      MethodArgumentTypeMismatchException e) {
    ErrorCode errorCode = ErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH;

    // 에러 상세 정보 구성 (requiredType, parameter, value)
    Map<String, Object> details = new HashMap<>();
    details.put("requiredType",
        e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "Unknown");
    details.put("parameter", e.getName());
    details.put("value", e.getValue());

    ErrorResponse error = ErrorResponse.builder()
        .timestamp(Instant.now())
        .code(errorCode.name())
        .message(errorCode.getMessage())
        .details(details)
        .exceptionType(e.getClass().getSimpleName())
        .status(errorCode.getHttpStatus().value())
        .build();

    log.warn("[TYPE_MISMATCH_EXCEPTION] ERROR_CODE={}, Message={}, details={}",
        error.code(),
        error.message(),
        error.details()
    );
    return ResponseEntity.status(error.status()).body(error);
  }

  // HTTP 메서드 오류
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(
      HttpRequestMethodNotSupportedException e) {
    ErrorCode errorCode = ErrorCode.METHOD_NOT_ALLOWED;

    ErrorResponse error = ErrorResponse.builder()
        .timestamp(Instant.now())
        .code(errorCode.name())
        .message(errorCode.getMessage())
        .exceptionType(e.getClass().getSimpleName())
        .status(errorCode.getHttpStatus().value())
        .build();

    log.warn("[HTTP_METHOD_EXCEPTION] ERROR CODE={}, Message={}",
        error.code(),
        error.message()
    );
    return ResponseEntity.status(error.status()).body(error);
  }

  // 그 외 서버 내부 오류
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception e) {
    ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;

    ErrorResponse error = ErrorResponse.builder()
        .timestamp(Instant.now())
        .code(errorCode.name())
        .message(errorCode.getMessage())
        .exceptionType(e.getClass().getSimpleName())
        .status(errorCode.getHttpStatus().value())
        .build();

    log.error("[UNEXPECTED_EXCEPTION] ERROR CODE={}, Message={}",
        error.code(),
        error.message(),
        e
    );
    return ResponseEntity.status(error.status()).body(error);
  }
}
