package com.codeit.mission.deokhugam.dashboard.popularreviews.dto;


import com.codeit.mission.deokhugam.review.entity.Review;
import java.util.List;

public record CursorPageResponsePopularReviewDto(
    List<PopularReviewDto> content,
    String nextCursor,
    String nextAfter,
    int size,
    long totalElements,
    boolean hasNext
) {

}
