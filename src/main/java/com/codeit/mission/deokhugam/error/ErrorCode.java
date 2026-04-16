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
    METHOD_ARGUMENT_TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "Method argument type mismatch"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error"),

    // 리뷰
    INVALID_REVIEW_RATING_RANGE(HttpStatus.BAD_REQUEST, "Rating must be between 1 and 5"),
    REVIEW_CONTENT_BLANK(HttpStatus.BAD_REQUEST, "Review content cannot be blank"),

    // 파워 유저 조회
    CURSOR_AFTER_NOT_PROVIDED_TOGETHER(HttpStatus.BAD_REQUEST, "Cursor and after must be provided together"),
    CURSOR_OR_AFTER_FORMAT_NOT_VALID(HttpStatus.BAD_REQUEST,"Invalid cursor or after format");
    private final HttpStatus httpStatus;
    private final String message;
}
