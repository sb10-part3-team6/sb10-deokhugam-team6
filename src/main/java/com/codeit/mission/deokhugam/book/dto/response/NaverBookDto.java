package com.codeit.mission.deokhugam.book.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.Builder;

@Schema(description = "네이버 도서 응답")
@Builder
public record NaverBookDto(
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
        description = "출판일",
        example = "2026-04-27"
    )
    LocalDate publishedDate,

    @Schema(
        description = "ISBN",
        example = "string"
    )
    String isbn,

    @Schema(
        description = "이미지",
        example = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII=",
        format = "byte"
    )
    byte[] thumbnailImage
) {

}
