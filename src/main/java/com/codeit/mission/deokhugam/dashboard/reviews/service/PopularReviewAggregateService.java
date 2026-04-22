package com.codeit.mission.deokhugam.dashboard.reviews.service;

import com.codeit.mission.deokhugam.comment.repository.CommentRepository;
import com.codeit.mission.deokhugam.dashboard.DirectionEnum;
import com.codeit.mission.deokhugam.dashboard.PeriodType;
import com.codeit.mission.deokhugam.dashboard.reviews.dto.CursorPageResponsePopularReviewDto;
import com.codeit.mission.deokhugam.dashboard.reviews.dto.ReviewCommentCount;
import com.codeit.mission.deokhugam.dashboard.reviews.dto.ReviewLikeCount;
import com.codeit.mission.deokhugam.dashboard.reviews.dto.ReviewStat;
import com.codeit.mission.deokhugam.dashboard.util.Utils;
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
  private final ReviewRepository reviewRepository;
  private final CommentRepository commentRepository;

  // 인기 리뷰 집계에 필요한 가중치
  private static final double COMMENT_COUNT_WEIGHT = 0.7d;
  private static final double LIKE_COUNT_WEIGHT = 0.3d;

  // 댓글, 리뷰 좋아요 레포지토리에서 스탯을 뽑아내고
  // 리뷰 아이디 별 인기 리뷰 스탯 형식으로 가공함.
  @Transactional(readOnly = true)
  public Map<UUID, ReviewStat> loadReviewStat(PeriodType periodType, LocalDateTime aggregatedAt){
    List<LocalDateTime> periods = Utils.calculatePeriod(periodType, aggregatedAt);

    Map<UUID, Long> reviewCommentCount = new HashMap<>();
    for(ReviewCommentCount item : commentRepository.findReviewCommentCounts(periods.get(0), periods.get(1))){
      reviewCommentCount.put(item.reviewId(), item.commentCount());
    }

    Map<UUID, Long> reviewLikedCount = new HashMap<>();
    for(ReviewLikeCount item : reviewRepository.countReviewLikes(periods.get(0))){
      reviewLikedCount.put(item.reviewId(), item.likeCount());
    }

    Set<UUID> reviewIds = new HashSet<>();
    reviewIds.addAll(reviewCommentCount.keySet());
    reviewIds.addAll(reviewLikedCount.keySet());

    Map<UUID, ReviewStat> statsByReviewId = new HashMap<>();

    for (UUID reviewId: reviewIds){
      long likeCount = reviewLikedCount.getOrDefault(reviewId, 0L);
      long commentCount = reviewCommentCount.getOrDefault(reviewId, 0L);

      statsByReviewId.put(reviewId, new ReviewStat(reviewId, likeCount, commentCount));
    }

    return statsByReviewId;
  }

}
