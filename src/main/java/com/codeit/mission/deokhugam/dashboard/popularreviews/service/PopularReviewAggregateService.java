package com.codeit.mission.deokhugam.dashboard.popularreviews.service;

import static java.lang.Double.NaN;

import com.codeit.mission.deokhugam.comment.repository.CommentRepository;
import com.codeit.mission.deokhugam.dashboard.PeriodType;
import com.codeit.mission.deokhugam.dashboard.popularreviews.dto.ReviewCommentCount;
import com.codeit.mission.deokhugam.dashboard.popularreviews.dto.ReviewLikeCount;
import com.codeit.mission.deokhugam.dashboard.popularreviews.dto.ReviewStat;
import com.codeit.mission.deokhugam.dashboard.popularreviews.entity.PopularReview;
import com.codeit.mission.deokhugam.dashboard.popularreviews.repository.PopularReviewRepository;
import com.codeit.mission.deokhugam.dashboard.util.Utils;
import com.codeit.mission.deokhugam.review.entity.ReviewStatus;
import com.codeit.mission.deokhugam.review.repository.ReviewRepository;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PopularReviewAggregateService {

  // 인기 리뷰 집계에 필요한 가중치
  //  private static final double COMMENT_COU
  private static final double COMMENT_COUNT_WEIGHT = 0.7d;
  private static final double LIKE_COUNT_WEIGHT = 0.3d;

  private final ReviewRepository reviewRepository;
  private final CommentRepository commentRepository;
  private final PopularReviewRepository popularReviewRepository;

  // 일괄적으로 리뷰의 점수를 로드
  @Transactional(readOnly = true)
  public Map<UUID, ReviewStat> loadReviewStat(PeriodType periodType, LocalDateTime aggregatedAt) {
    List<LocalDateTime> periods = Utils.calculatePeriod(periodType, aggregatedAt);

    Map<UUID, Long> reviewCommentCounts = new HashMap<>();
    for (ReviewCommentCount item : commentRepository.findReviewCommentCounts(periods.get(0), periods.get(1))) {
      reviewCommentCounts.put(item.reviewId(), item.commentCount());
    }

    Map<UUID, Long> reviewLikeCounts = new HashMap<>();
    for (ReviewLikeCount item
        : reviewRepository.countReviewLikes(periods.get(0), periods.get(1), ReviewStatus.ACTIVE)) {
      reviewLikeCounts.put(item.reviewId(), item.likeCount());
    }

    Set<UUID> reviewIds = new HashSet<>();
    reviewIds.addAll(reviewCommentCounts.keySet());
    reviewIds.addAll(reviewLikeCounts.keySet());

    Map<UUID, ReviewStat> statsByReviewId = new HashMap<>();
    for (UUID reviewId : reviewIds) {
      long likeCount = reviewLikeCounts.getOrDefault(reviewId, 0L);
      long commentCount = reviewCommentCounts.getOrDefault(reviewId, 0L);

      statsByReviewId.put(reviewId, new ReviewStat(reviewId, likeCount, commentCount));
    }

    return statsByReviewId;
  }

  @Transactional
  public void rankPopularReviews(PeriodType periodType, LocalDateTime aggregatedAt, UUID snapshotId) {
    List<LocalDateTime> periods = Utils.calculatePeriod(periodType, aggregatedAt);

    List<PopularReview> popularReviews =
        popularReviewRepository.findByPeriodDescByScore(
            periodType, periods.get(0), periods.get(1), snapshotId);
    long rank = 1L;
    double previousScore = NaN;
    long index = 1L;

    for (PopularReview popularReview : popularReviews) {
      if (index == 1L || Double.compare(popularReview.getScore(), previousScore) != 0) {
        rank = index;
        previousScore = popularReview.getScore();
      }
      popularReview.updateRank(rank);
      index++;
    }
  }

  public PopularReview toPopularReview(
      UUID reviewId,
      ReviewStat stat,
      PeriodType periodType,
      LocalDateTime aggregatedAt,
      UUID snapshotId
  ) {
    LocalDateTime periodStart = periodType.calculateStart(aggregatedAt);
    LocalDateTime periodEnd = periodType.calculateEnd(aggregatedAt);

    return PopularReview.builder()
        .reviewId(reviewId)
        .periodType(periodType)
        .periodStart(periodStart)
        .periodEnd(periodEnd)
        .rank(0L)
        .score(calculateScore(stat.likeCount(), stat.commentCount()))
        .likeCount(stat.likeCount())
        .commentCount(stat.commentCount())
        .aggregatedAt(aggregatedAt)
        .snapshotId(snapshotId)
        .build();
  }


  public ReviewStat emptyStat(UUID reviewId) {
    return new ReviewStat(reviewId, 0L, 0L);
  }


  private double calculateScore(long likeCount, long commentCount) {
    return (likeCount * LIKE_COUNT_WEIGHT) + (commentCount * COMMENT_COUNT_WEIGHT);
  }
}
