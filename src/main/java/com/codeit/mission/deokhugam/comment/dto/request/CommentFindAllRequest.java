package com.codeit.mission.deokhugam.comment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "조회할 댓글 목록 정보")
public record CommentFindAllRequest(
    @Schema(
        description = "대상 리뷰 ID",
        example = "123e4567-e89b-12d3-a456-426614174000",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull
    UUID reviewId,

    @Schema(
        description = "정렬 방향",
        allowableValues = {"ASC", "DESC"},
        defaultValue = "DESC",
        example = "DESC"
    )
    @Pattern(regexp = "^(ASC|DESC)$", message = "direction은 ASC 또는 DESC만 가능합니다.")
    String direction,

    @Schema(
        description = "커서 페이지네이션 커서",
        example = "string"
    )
    String cursor,

    @Schema(
        description = "보조 커서 (createdAt)",
        example = "2025-04-06T15:04:05.000z"
    )
    Instant after,

    @Schema(
        description = "페이지 크기",
        defaultValue = "50",
        example = "50"
    )
    @Min(value = 1, message = "값은 최소 1 이상이어야 합니다.")
    @Max(value = 100, message = "값은 최대 100 이하여야 합니다.")
    Integer limit
) {

  public CommentFindAllRequest {
    if (limit == null) {
      limit = 50;
    }

    if (direction == null) {
      direction = "DESC";
    }
  }
}
