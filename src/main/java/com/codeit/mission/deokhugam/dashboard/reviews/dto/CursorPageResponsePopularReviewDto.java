package com.codeit.mission.deokhugam.dashboard.reviews.dto;


import com.codeit.mission.deokhugam.review.entity.Review;
import java.util.List;

public record CursorPageResponsePopularReviewDto(
    List<Review> content,
    String nextCursor,
    String nextAfter,
    int size,
    long totalElements,
    boolean hasNext
) {

}
