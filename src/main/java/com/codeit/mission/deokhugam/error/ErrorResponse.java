package com.codeit.mission.deokhugam.error;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.Instant;
import java.util.Map;

@Schema(description = "공통 예외 응답")
@Builder
public record ErrorResponse(
    @Schema(
        description = "에러 발생 시점",
        example = "2025-04-06T15:04:05.000Z"
    )
    Instant timestamp,

    @Schema(
        description = "발생한 에러 코드",
        example = "ERROR_CODE"
    )
    String code,

    @Schema(
        description = "에러 메시지",
        example = "string"
    )
    String message,

    @Schema(
        description = "관련 추가 정보",
        example = "{\"string\": \"object\"}"
    )
    Map<String, Object> details,

    @Schema(
        description = "발생한 예외 클래스명",
        example = "ErrorException"
    )
    String exceptionType,

    @Schema(
        description = "HTTP 상태 코드",
        example = "400"
    )
    int status
) {

}