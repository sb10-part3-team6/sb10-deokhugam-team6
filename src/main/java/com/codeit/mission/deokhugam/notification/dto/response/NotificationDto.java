package com.codeit.mission.deokhugam.notification.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Schema(description = "알림 응답")
@Builder
public record NotificationDto(
    @Schema(
        description = "알림 ID",
        example = "3fa85f64-5717-4562-b3fc-2c963f66afa6"
    )
    UUID id,

    @Schema(
        description = "사용자 ID",
        example = "3fa85f64-5717-4562-b3fc-2c963f66afa6"
    )
    UUID userId,

    @Schema(
        description = "리뷰 ID",
        example = "3fa85f64-5717-4562-b3fc-2c963f66afa6"
    )
    UUID reviewId,

    @Schema(
        description = "리뷰 내용",
        example = "string"
    )
    String reviewContent,

    @Schema(
        description = "매사자",
        example = "string"
    )
    String message,

    @Schema(
        description = "상태",
        example = "true"
    )
    boolean confirmed,

    @Schema(
        description = "생성 시점",
        example = "2026-04-24T02:49:51.932Z"
    )
    Instant createdAt,

    @Schema(
        description = "수정 시점",
        example = "2026-04-24T02:49:51.932Z"
    )
    Instant updatedAt
) {

}
