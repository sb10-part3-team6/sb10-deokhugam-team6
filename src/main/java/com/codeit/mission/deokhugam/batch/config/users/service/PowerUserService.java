package com.codeit.mission.deokhugam.batch.config.users;

import com.codeit.mission.deokhugam.batch.config.PeriodType;
import com.codeit.mission.deokhugam.batch.config.users.dto.PowerUserLikeCount;
import com.codeit.mission.deokhugam.batch.config.users.dto.PowerUserDto;
import com.codeit.mission.deokhugam.batch.config.users.dto.PowerUserReceivedLikeCount;
import com.codeit.mission.deokhugam.batch.config.users.entity.PowerUser;
import com.codeit.mission.deokhugam.batch.config.users.repository.PowerUserRepository;
import com.codeit.mission.deokhugam.batch.config.users.repository.ReviewLikeRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PowerUserService {

  private static final double REVIEW_SCORE_WEIGHT = 0.5d;
  private static final double LIKE_COUNT_WEIGHT = 0.2d;
  private static final double RECEIVED_REVIEW_LIKE_WEIGHT = 0.3d;

  private final ReviewLikeRepository reviewLikeRepository;
  private final PowerUserRepository powerUserRepository;

  @Transactional(readOnly = true)
  public List<PowerUserDto> getLatestRankings(PeriodType periodType) {
    return powerUserRepository.findLatestRankingDtosByPeriodType(periodType);
  }

  @Transactional
  public void aggregate(PeriodType periodType, LocalDateTime aggregatedAt) {
    LocalDateTime periodStart = periodType.calculateStart(aggregatedAt);
    LocalDateTime periodEnd = periodType.calculateEnd(aggregatedAt);

    Map<UUID, PowerUserMetrics> metricsByUserId = new HashMap<>();

    for (PowerUserReceivedLikeCount receivedLikeCount :
        reviewLikeRepository.countReceivedLikesByReviewAuthor(periodStart, periodEnd)) {
      PowerUserMetrics metrics =
          metricsByUserId.computeIfAbsent(receivedLikeCount.userId(), ignored -> new PowerUserMetrics());
      metrics.addReviewScore(receivedLikeCount.likeCount() * RECEIVED_REVIEW_LIKE_WEIGHT);
    }

    for (PowerUserLikeCount likeCount :
        reviewLikeRepository.countLikesGroupedByUser(periodStart, periodEnd)) {
      PowerUserMetrics metrics =
          metricsByUserId.computeIfAbsent(likeCount.userId(), ignored -> new PowerUserMetrics());
      metrics.addLikeCount(likeCount.likeCount());
    }

    List<Map.Entry<UUID, PowerUserMetrics>> rankedEntries =
        metricsByUserId.entrySet().stream()
            .peek(entry -> entry.getValue().computeScore(REVIEW_SCORE_WEIGHT, LIKE_COUNT_WEIGHT))
            .filter(entry -> entry.getValue().score() > 0d)
            .sorted(
                Comparator
                    .<Map.Entry<UUID, PowerUserMetrics>>comparingDouble(entry -> entry.getValue().score())
                    .reversed()
                    .thenComparing(entry -> entry.getKey().toString()))
            .toList();

    List<PowerUser> rankings = new ArrayList<>();
    for (int i = 0; i < rankedEntries.size(); i++) {
      Map.Entry<UUID, PowerUserMetrics> entry = rankedEntries.get(i);
      PowerUserMetrics metrics = entry.getValue();
      rankings.add(
          PowerUser.builder()
              .userId(entry.getKey())
              .periodType(periodType)
              .periodStart(periodStart)
              .periodEnd(periodEnd)
              .rank((long) i + 1L)
              .score(metrics.score())
              .reviewScoreSum(metrics.reviewScoreSum())
              .likeCount(metrics.likeCount())
              .commentCount(0L)
              .aggregatedAt(aggregatedAt)
              .build());
    }

    powerUserRepository.deleteByPeriodTypeAndPeriodStartAndPeriodEnd(periodType, periodStart, periodEnd);
    powerUserRepository.saveAll(rankings);
  }

  private static final class PowerUserMetrics {
    private double reviewScoreSum;
    private long likeCount;
    private double score;

    void addReviewScore(double reviewScore) {
      this.reviewScoreSum += reviewScore;
    }

    void addLikeCount(long likeCount) {
      this.likeCount += likeCount;
    }

    void computeScore(double reviewScoreWeight, double likeCountWeight) {
      this.score = (reviewScoreSum * reviewScoreWeight) + (likeCount * likeCountWeight);
    }

    double reviewScoreSum() {
      return reviewScoreSum;
    }

    long likeCount() {
      return likeCount;
    }

    double score() {
      return score;
    }
  }
}
