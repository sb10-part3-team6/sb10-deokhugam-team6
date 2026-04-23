package com.codeit.mission.deokhugam.dashboard.popularreviews.dto;

import com.codeit.mission.deokhugam.dashboard.PeriodType;
import java.time.LocalDateTime;
import java.util.UUID;

public record PopularReviewDto(
    UUID id,
    UUID reviewId,
    UUID bookId,
    String bookTitle,
    String bookThumbnailUrl,
    UUID userId,
    String userNickname,
    String reviewContent,
    double reviewRating,
    PeriodType period,
    LocalDateTime createdAt,
    long rank,
    double score,
    long likeCount,
    long commentCount
) {

}
