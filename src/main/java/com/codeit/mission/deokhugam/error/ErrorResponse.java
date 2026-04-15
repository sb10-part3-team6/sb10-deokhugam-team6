package com.codeit.mission.deokhugam.error;

import lombok.Builder;

import java.time.Instant;
import java.util.Map;

/*
    에러 응답 DTO
    ----------------
    공통된 예외 메시지 처리를 위한 응답 DTO
 */
@Builder
public record ErrorResponse (
        Instant timestamp,                  // 에러 발생 시각
        String code,                        // 발생한 에러 코드
        String message,                     // 발생한 에러 메시지
        Map<String, Object> details,        // 발생한 에러와 관련된 추가 정보
        String exceptionType,               // 발생한 에러 클래스 이름
        int status                          // HTTP 상태 코드
) {
}