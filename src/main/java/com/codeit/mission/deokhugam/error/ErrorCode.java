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
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "Invalid input value"),                             // 입력값 오류
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "Method not allowed"),                        // HTTP 메서드 오류
    MISSING_REQUEST_PARAMETER(HttpStatus.BAD_REQUEST, "Missing request parameter"),                 // 필수 파라미터 누락
    METHOD_ARGUMENT_TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "Method argument type mismatch"),         // 파라미터 타입 불일치
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error"),               // 서버 내부 오류

    // 유저 (로그인/회원가입)
    LOGIN_INPUT_INVALID(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
    EMAIL_DUPLICATION(HttpStatus.CONFLICT, "이미 존재하는 이메일입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),

    // 리뷰
    INVALID_REVIEW_RATING_RANGE(HttpStatus.BAD_REQUEST, "Rating must be between 1 and 5"),          // 평점 범위(1~5) 이탈
    REVIEW_CONTENT_BLANK(HttpStatus.BAD_REQUEST, "Review content cannot be blank"),                 // 평점 내용 공백
    DUPLICATE_REVIEWS(HttpStatus.CONFLICT, "Review with BookId and UserId already exists"),         // 사용의 특정 도서 리뷰 중복
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "Review with Id not found"),                             // 특정 리뷰 조회 실패
    REVIEW_AUTHOR_MISMATCH(HttpStatus.FORBIDDEN, "Review author mismatch with requestUserId"),      // 요청자와 리뷰 작성자 불일치

    // 파워 유저 조회
    CURSOR_AFTER_NOT_PROVIDED_TOGETHER(HttpStatus.BAD_REQUEST, "Cursor and after must be provided together"),
    CURSOR_OR_AFTER_FORMAT_NOT_VALID(HttpStatus.BAD_REQUEST,"Invalid cursor or after format"),

    //도서
    WRONG_FILE_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Wrong file type"),
    S3_UPLOAD_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "S3 upload failed"),
    INVALID_ISBN(HttpStatus.BAD_REQUEST, "Invalid ISBN"),
    DUPLICATE_ISBN(HttpStatus.CONFLICT, "Duplicate ISBN"),
    BOOK_NOT_FOUND(HttpStatus.NOT_FOUND, "Book not found"),
    EXTERNAL_API_ERROR(HttpStatus.BAD_GATEWAY, "외부 API 요청 오류"),

    // 댓글
    FORBIDDEN_COMMENT_UPDATE(HttpStatus.FORBIDDEN, "댓글 수정 권한이 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
