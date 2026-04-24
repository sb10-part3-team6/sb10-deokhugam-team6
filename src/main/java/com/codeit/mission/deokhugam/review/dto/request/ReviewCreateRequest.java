package com.codeit.mission.deokhugam.review.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "생성할 리뷰 정보")
public record ReviewCreateRequest(
    @Schema(
        description = "대상 도서 ID",
        example = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "리뷰 대상 도서는 필수입니다.")
    UUID bookId,

    @Schema(
        description = "작성자 ID",
        example = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "리뷰 작성자는 필수입니다.")
    UUID userId,

    @Schema(
        description = "내용",
        example = "string",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "리뷰 내용은 비워둘 수 없습니다.")
    String content,

    @Schema(
        description = "평점",
        example = "1",
        minimum = "1",
        maximum = "5",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "평점이 필요합니다.")
    @Min(value = 1, message = "평점의 최솟값은 1입니다.")
    @Max(value = 5, message = "평점의 최댓값은 5입니다.")
    Integer rating
) {

}
