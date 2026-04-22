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
  INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST,
      "Invalid input value"),                             // 입력값 오류
  METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED,
      "Method not allowed"),                              // HTTP 메서드 오류
  MISSING_REQUEST_PARAMETER(HttpStatus.BAD_REQUEST,
      "Missing request parameter"),                       // 필수 파라미터 누락
  METHOD_ARGUMENT_TYPE_MISMATCH(HttpStatus.BAD_REQUEST,
      "Method argument type mismatch"),                   // 파라미터 타입 불일치
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,
      "Internal server error"),                           // 서버 내부 오류

  // 유저 (로그인/회원가입)
  LOGIN_INPUT_INVALID(HttpStatus.UNAUTHORIZED, "Invalid email or password"),
  EMAIL_DUPLICATION(HttpStatus.CONFLICT, "Email address already exists"),
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User not found"),

  // 리뷰
  INVALID_REVIEW_RATING_RANGE(HttpStatus.BAD_REQUEST,
      "Rating must be between 1 and 5"),                       // 평점 범위(1~5) 이탈
  REVIEW_CONTENT_BLANK(HttpStatus.BAD_REQUEST,
      "Review content cannot be blank"),                       // 평점 내용 공백
  DUPLICATE_REVIEWS(HttpStatus.CONFLICT,
      "Review with BookId and UserId already exists"),         // 사용의 특정 도서 리뷰 중복
  REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND,
      "Review with Id not found"),                             // 특정 리뷰 조회 실패
  REVIEW_AUTHOR_MISMATCH(HttpStatus.FORBIDDEN,
      "Review author mismatch with requestUserId"),            // 요청자와 리뷰 작성자 불일치
  DUPLICATE_REVIEW_LIKE_REQUEST(HttpStatus.CONFLICT,
      "Review like request already exists"),                   // 특정 도서에 대한 좋아요 추가 생성 요청 존재
  REVIEW_LIKE_NOT_FOUND(HttpStatus.NOT_FOUND,
      "Review like with Id not found"),                        // 특정 도서에 대한 사용자의 좋아요 조회 실패
  REQUEST_USER_MISMATCH(HttpStatus.BAD_REQUEST,
      "Parameter and Header requestUserId mismatch"),          // 쿼리문 요청자와 헤더 내 요청자 불일치
  INVALID_CURSOR_FORMAT(HttpStatus.BAD_REQUEST,
      "cursor format is invalid"),                             // 잘못된 커서 형식

  // 파워 유저 집계 및 조회
  CURSOR_AFTER_NOT_PROVIDED_TOGETHER(HttpStatus.BAD_REQUEST,
      "Cursor and after must be provided together"),
  CURSOR_OR_AFTER_FORMAT_NOT_VALID(HttpStatus.BAD_REQUEST, "Invalid cursor or after format"),
  POWER_AGGREGATION_BATCH_JOB_FAILED(HttpStatus.INTERNAL_SERVER_ERROR,
      "PowerUser Aggregation Batch job Failed"),
  SNAPSHOT_NOT_FOUND(HttpStatus.NOT_FOUND, "Snapshot is not found"),
  INVALID_JOB_PARAMETER(HttpStatus.BAD_REQUEST, "Batch Job Parameter is Invalid"),

    //도서
    WRONG_FILE_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Wrong file type"),
    S3_UPLOAD_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "S3 upload failed"),
    INVALID_ISBN(HttpStatus.BAD_REQUEST, "Invalid ISBN"),
    DUPLICATE_ISBN(HttpStatus.CONFLICT, "Duplicate ISBN"),
    BOOK_NOT_FOUND(HttpStatus.NOT_FOUND, "Book not found"),
    EXTERNAL_API_ERROR(HttpStatus.BAD_GATEWAY, "external api error"),
    OCR_DETECT_FAILED(HttpStatus.BAD_REQUEST, "OCR detection failed"),
    S3_URL_PARSE_FAILED(HttpStatus.BAD_REQUEST, "S3 url parsing failed"),

  // 댓글
  FORBIDDEN_COMMENT_UPDATE(HttpStatus.FORBIDDEN, "Permission denied to edit this comment");

  private final HttpStatus httpStatus;
  private final String message;
}
