package com.codeit.mission.deokhugam.book.dto.request;

import com.codeit.mission.deokhugam.book.entity.SortDirection;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "검색할 도서 정보")
public record BookSearchConditionDto(
    @Schema(
        description = "검색 키워드 (도서 제목 | 저자 | ISBN)",
        example = "홍길동"
    )
    String keyword,

    @Schema(
        description = "정렬 기준 (title | publishedDate | rating | reviewCount)",
        allowableValues = {"title", "publishedDate", "rating", "reviewCount"},
        defaultValue = "title",
        example = "title"
    )
    String orderBy,

    @Schema(
        description = "정렬 방향",
        allowableValues = {"ASC", "DESC"},
        defaultValue = "DESC",
        example = "DESC"
    )
    SortDirection direction,

    @Schema(
        description = "커서 페이지네이션 커서",
        example = "string"
    )
    Object cursor,

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
  public BookSearchConditionDto {
    if (orderBy == null) {
      orderBy = "title";
    }
    if (direction == null) {
      direction = SortDirection.DESC;
    }
    if (limit == null || limit <= 0) {
      limit = 50;
    }
    limit = Math.min(limit, 50);
  }
}
