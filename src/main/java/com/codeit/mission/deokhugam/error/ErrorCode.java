package com.codeit.mission.deokhugam.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/*
    커스텀 예외 코드
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
  // 공통
  INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "Invalid input value"),
  METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "Method not allowed"),
  MISSING_REQUEST_PARAMETER(HttpStatus.BAD_REQUEST, "Missing request parameter"),
  INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "잘못된 타입이 입력되었습니다."),
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error"),

  // 유저 (로그인/회원가입)
  LOGIN_INPUT_INVALID(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
  EMAIL_DUPLICATION(HttpStatus.CONFLICT, "이미 존재하는 이메일입니다."),
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),

    // 리뷰
    INVALID_REVIEW_RATING_RANGE(HttpStatus.BAD_REQUEST, "Rating must be between 1 and 5"),
    REVIEW_CONTENT_BLANK(HttpStatus.BAD_REQUEST, "Review content cannot be blank"),

    // 파워 유저 조회
    CURSOR_AFTER_NOT_PROVIDED_TOGETHER(HttpStatus.BAD_REQUEST, "Cursor and after must be provided together"),
    CURSOR_OR_AFTER_FORMAT_NOT_VALID(HttpStatus.BAD_REQUEST,"Invalid cursor or after format"),

    //도서
    WRONG_FILE_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Wrong file type"),
    S3_UPLOAD_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "S3 upload failed"),
    INVALID_ISBN(HttpStatus.BAD_REQUEST, "Invalid ISBN"),
    DUPLICATE_ISBN(HttpStatus.CONFLICT, "Duplicate ISBN"),
    BOOK_NOT_FOUND(HttpStatus.NOT_FOUND, "Book not found"),
    EXTERNAL_API_ERROR(HttpStatus.BAD_GATEWAY, "외부 API 요청 오류");

    private final HttpStatus httpStatus;
    private final String message;
}
