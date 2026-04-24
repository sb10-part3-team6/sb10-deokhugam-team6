package com.codeit.mission.deokhugam.review.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


@Schema(description = "수정할 리뷰 정보를 담보 DTO")
public record ReviewUpdateRequest(
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
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "평점이 필요합니다.")
    @Min(value = 1, message = "평점의 최솟값은 1입니다.")
    @Max(value = 5, message = "평점의 최댓값은 5입니다.")
    Integer rating
) {

}
