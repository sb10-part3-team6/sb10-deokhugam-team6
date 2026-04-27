package com.codeit.mission.deokhugam.review.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "목록 조회할 리뷰 정보")
public record ReviewSearchConditionDto(
    @Schema(
        description = "작성자 ID",
        example = "123e4567-e89b-12d3-a456-426614174000"
    )
    UUID userId,

    @Schema(
        description = "대상 도서 ID",
        example = "123e4567-e89b-12d3-a456-426614174000"
    )
    UUID bookId,

    @Schema(
        description = "검색 키워드 (작성자 닉네임 | 내용)",
        example = "홍길동"
    )
    String keyword,

    @Schema(
        description = "정렬 기준 (createdAt | rating)",
        allowableValues = {"createdAt", "rating"},
        defaultValue = "createdAt",
        example = "createdAt"
    )
    String orderBy,

    @Schema(
        description = "정렬 방향",
        allowableValues = {"ASC", "DESC"},
        defaultValue = "DESC",
        example = "DESC"
    )
    String direction,

    @Schema(
        description = "커서 페이지네이션 커서",
        example = "string"
    )
    String cursor,

    @Schema(
        description = "보조 커서 (createdAt)",
        example = "2025-04-06T15:04:05.000Z"
    )
    Instant after,

    @Schema(
        description = "페이지 크기",
        defaultValue = "50",
        example = "50"
    )
    Integer limit
) {

  // 기본값 설정을 위한 생성자
  public ReviewSearchConditionDto {
    if (orderBy == null) {
      orderBy = "created_at";
    }
    if (direction == null) {
      direction = "desc";
    }
    if (limit == null || limit <= 0) {
      limit = 50;
    }
    limit = Math.min(limit, 50);
  }
}
