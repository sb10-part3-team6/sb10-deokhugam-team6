package com.codeit.mission.deokhugam.book.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Schema(description = "생성할 도서 정보")
public record BookCreateRequest(
    @Schema(
        description = "도서명",
        example = "string",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "제목을 입력해주세요")
    String title,

    @Schema(
        description = "저자",
        example = "string",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "지은이를 입력해주세요.")
    String author,

    @Schema(
        description = "설명",
        example = "string",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "설명을 최소 1글자 이상 적어주세요.")
    String description,

    @Schema(
        description = "출판사",
        example = "string",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "출판사 정보를 입력해주세요.")
    String publisher,

    @Schema(
        description = "출판일",
        example = "2026-04-27",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "출판 일자를 적어주세요.")
    LocalDate publishedDate,

    @Schema(
        description = "ISBN",
        example = "string"
    )
    String isbn
) {

}
