package com.codeit.mission.deokhugam.review.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/*
        리뷰 수정 요청 DTO
 */
public record ReviewUpdateRequest (
        @NotBlank(message = "리뷰 내용은 비워둘 수 없습니다.")
        String content,                                         // 수정할 리뷰 내용

        @NotNull(message = "평점이 필요합니다.")
        @Min(value = 1, message = "평점의 최솟값은 1입니다.")
        @Max(value = 5, message = "평점의 최댓값은 5입니다.")
        Integer rating                                           // 수정할 리뷰 평점
) {
}
