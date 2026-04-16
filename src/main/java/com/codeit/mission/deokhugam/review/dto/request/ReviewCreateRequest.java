package com.codeit.mission.deokhugam.review.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/*
        리뷰 생성 요청 DTO
 */
public record ReviewCreateRequest (
        @NotNull(message = "리뷰 대상 도서는 필수입니다.")
        UUID bookId,                                            // 리뷰 대상 도서

        @NotNull(message = "리뷰 작성자는 필수입니다.")
        UUID userId,                                            // 리뷰 작성자

        @NotBlank(message = "리뷰 내용은 비워둘 수 없습니다.")
        String content,                                         // 리뷰 내용

        @NotNull(message = "평점이 필요합니다.")
        @Min(value = 1, message = "평점의 최솟값은 1입니다.")
        @Max(value = 5, message = "평점의 최댓값은 5입니다.")
        Integer rating                                          // 리뷰 평점
){
}
