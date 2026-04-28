package com.codeit.mission.deokhugam.book.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "도서 응답")
public record BookDto(
    @Schema(
        description = "도서 ID",
        example = "3fa85f64-5717-4562-b3fc-2c963f66afa6"
    )
    UUID id,

    @Schema(
        description = "도서명",
        example = "string"
    )
    String title,

    @Schema(
        description = "저자",
        example = "string"
    )
    String author,

    @Schema(
        description = "설명",
        example = "string"
    )
    String description,

    @Schema(
        description = "출판사",
        example = "string"
    )
    String publisher,

    @Schema(
        description = "출판일 (yyyy-mm-dd)",
        format = "date",
        example = "2026-04-27"
    )
    LocalDate publishedDate,

    @Schema(
        description = "국제 표준 도서 번호 (ISBN)",
        example = "9788960771291"
    )
    String isbn,

    @Schema(
        description = "URL 주소",
        example = "http://codeit.com"
    )
    String thumbnailUrl,

    @Schema(
        description = "리뷰 개수",
        example = "1"
    )
    int reviewCount,

    @Schema(
        description = "평점",
        example = "1.0"
    )
    double rating,

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
