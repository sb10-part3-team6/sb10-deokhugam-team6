package com.codeit.mission.deokhugam.dashboard.popularreviews.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record CursorPageResponsePopularReviewDto(
    @Schema(
        description = "페이지 내용"
    )
    List<PopularReviewDto> content,

    @Schema(
        description = "다음 페이지 커서",
        example = "string"
    )
    String nextCursor,
    

    String nextAfter,
    int size,
    long totalElements,
    boolean hasNext
) {

}
