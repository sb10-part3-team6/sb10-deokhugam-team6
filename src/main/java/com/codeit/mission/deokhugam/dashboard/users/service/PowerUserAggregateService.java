package com.codeit.mission.deokhugam.dashboard.users.service;

import static java.lang.Double.NaN;

import com.codeit.mission.deokhugam.comment.repository.CommentRepository;
import com.codeit.mission.deokhugam.dashboard.PeriodType;
import com.codeit.mission.deokhugam.dashboard.users.dto.PowerUserLikeCount;
import com.codeit.mission.deokhugam.dashboard.users.dto.UserCommentCount;
import com.codeit.mission.deokhugam.dashboard.users.dto.UserReviewAggregate;
import com.codeit.mission.deokhugam.dashboard.users.dto.UserStat;
import com.codeit.mission.deokhugam.dashboard.users.entity.PowerUser;
import com.codeit.mission.deokhugam.dashboard.users.repository.PowerUserRepository;
import com.codeit.mission.deokhugam.dashboard.users.repository.ReviewLikeRepository;
import com.codeit.mission.deokhugam.review.entity.ReviewStatus;
import com.codeit.mission.deokhugam.review.repository.ReviewRepository;
import com.codeit.mission.deokhugam.user.entity.User;
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
public class PowerUserAggregateService {

  private static final double REVIEW_SCORE_WEIGHT = 0.5d;
  private static final double LIKE_COUNT_WEIGHT = 0.2d;
  private static final double COMMENT_COUNT_WEIGHT = 0.3d;

  private final CommentRepository commentRepository;
  private final ReviewRepository reviewRepository;
  private final ReviewLikeRepository reviewLikeRepository;
  private final PowerUserRepository powerUserRepository;

  // 기간 내 댓글/리뷰/좋아요를 userId 기준으로 벌크 집계한다.
  // 소스별 집계를 분리해 조인으로 인한 중복 합산을 피하고,
  // process 단계의 per-user 조회를 제거한다.
  @Transactional(readOnly = true)
  public Map<UUID, UserStat> loadUserStats(PeriodType periodType, LocalDateTime aggregatedAt) {
    // periodType과 aggregatedAt 기준으로 산정할 기간을 측정
    LocalDateTime periodStart = periodType.calculateStart(aggregatedAt);
    LocalDateTime periodEnd = periodType.calculateEnd(aggregatedAt);

    // <유저 ID, 댓글 수> 형태의 Map
    Map<UUID, Long> commentCounts = new HashMap<>();

    // commentRepository에서 User별 댓글 수를 뽑아온 다음, 순회
    for (UserCommentCount commentCount : commentRepository.findUserCommentCounts(periodStart, periodEnd)) {
      commentCounts.put(commentCount.userId(), commentCount.commentCount()); // commentCounts 해쉬맵에 삽입
    }

    // <유저 ID, 유저 리뷰 점수> 형태의 Map
    Map<UUID, Double> reviewScoreSums = new HashMap<>();

    // reviewRepository에서 User별 리뷰 점수의 합계를 뽑아온 다음 순회함.
    for (UserReviewAggregate reviewAggregate
        : reviewRepository.findUserReviewAggregates(periodStart, periodEnd, ReviewStatus.ACTIVE)) {
      reviewScoreSums.put(reviewAggregate.userId(), reviewAggregate.reviewScoreSum());
    }

    // 유저 ID, 좋아요 개수> 형태의 Map
    // 추후 리뷰 도메인 쪽에서 좋아요 관련 기능을 보고 더 고도화 할 예정
    Map<UUID, Long> likeCounts = new HashMap<>();
    // reviewLikeRepository에서 User별 누른 좋아요 수를 뽑아온 다음 순회함.
    for (PowerUserLikeCount likeCount : reviewLikeRepository.findUserLikeCounts(periodStart, periodEnd)) {
      likeCounts.put(likeCount.userId(), likeCount.likeCount());
    }

    // 중복을 허용하지 않도록 HashSet을 이용해 위의 과정에서 얻은 유저 ID를 집어넣음
    Set<UUID> userIds = new HashSet<>();
    userIds.addAll(commentCounts.keySet());
    userIds.addAll(reviewScoreSums.keySet());
    userIds.addAll(likeCounts.keySet());

    // <유저 ID, 유저 스탯> Map
    Map<UUID, UserStat> statsByUserId = new HashMap<>();

    for (UUID userId : userIds) {
      // UserId별 스탯을 이전에 작성했던 도메인 별 Map에서 가져온다.
      double reviewScoreSum = reviewScoreSums.getOrDefault(userId, 0d);
      long likeCount = likeCounts.getOrDefault(userId, 0L);
      long commentCount = commentCounts.getOrDefault(userId, 0L);

      // 유저 별 유저스탯을 삽입
      statsByUserId.put(
          userId,
          new UserStat(
              userId,
              reviewScoreSum,
              likeCount,
              commentCount,
              calculateScore(reviewScoreSum, likeCount, commentCount)));
    }
    // 모든 유저 별 스탯 Map을 리턴함.
    return statsByUserId;
  }

  @Transactional
  public void rankPowerUsers(PeriodType periodType, LocalDateTime aggregatedAt, UUID snapshotId) {
    LocalDateTime periodStart = periodType.calculateStart(aggregatedAt);
    LocalDateTime periodEnd = periodType.calculateEnd(aggregatedAt);

    List<PowerUser> powers =
        powerUserRepository.findByPeriodDescByScore(periodType, periodStart, periodEnd, snapshotId);
    long rank = 1L;
    double previousScore = NaN;
    long index = 1L;

    for (PowerUser powerUser : powers) {
      if (index == 1L || Double.compare(powerUser.getScore(), previousScore) != 0) {
        rank = index;
        previousScore = powerUser.getScore();
      }
      powerUser.updateRank(rank);
      index++;
    }
  }

  // User에서 PowerUser로 변환하는 메서드
  public PowerUser toPowerUser(
      User user,
      UserStat stat,
      PeriodType periodType,
      LocalDateTime aggregatedAt,
      UUID snapshotId) {

    LocalDateTime periodStart = periodType.calculateStart(aggregatedAt);
    LocalDateTime periodEnd = periodType.calculateEnd(aggregatedAt);

    return PowerUser.builder()
        .userId(user.getId())
        .periodType(periodType)
        .periodStart(periodStart)
        .periodEnd(periodEnd)
        .rank(0L)
        .score(stat.score())
        .commentCount(stat.commentCount())
        .reviewScoreSum(stat.reviewScoreSum())
        .likeCount(stat.likeCount())
        .aggregatedAt(aggregatedAt)
        .snapshotId(snapshotId)
        .build();
  }

  public UserStat emptyStat(UUID userId) {
    return new UserStat(userId, 0d, 0L, 0L, 0d);
  }

  // 활동 점수를 계산하는 private 메서드
  private double calculateScore(double reviewScoreSum, long likeCount, long commentCount) {
    return (reviewScoreSum * REVIEW_SCORE_WEIGHT)
        + (commentCount * COMMENT_COUNT_WEIGHT)
        + (likeCount * LIKE_COUNT_WEIGHT);
  }
}
