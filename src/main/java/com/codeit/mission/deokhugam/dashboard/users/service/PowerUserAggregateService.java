package com.codeit.mission.deokhugam.dashboard.users.service;

import static java.lang.Double.NaN;

import com.codeit.mission.deokhugam.dashboard.PeriodType;
import com.codeit.mission.deokhugam.dashboard.users.dto.UserStat;
import com.codeit.mission.deokhugam.dashboard.users.entity.PowerUser;
import com.codeit.mission.deokhugam.dashboard.users.repository.PowerUserRepository;
import com.codeit.mission.deokhugam.user.entity.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PowerUserAggregateService {

  // 파워 유저 계산에 필요한 가중치들.
  private static final double REVIEW_SCORE_WEIGHT = 0.5d;
  private static final double LIKE_COUNT_WEIGHT = 0.2d;
  private static final double COMMENT_COUNT_WEIGHT = 0.3d;

  // private final ReviewRepository reviewRepository; <- 추후 구현 시 주석 제거
  // private final CommentRepository commentRepository;
  // private final LikeRepository likeRepository <- 좋아요도 레포가 존재한다는 가정 하에 설계함
  private final PowerUserRepository powerUserRepository;

  // 유저 ID를 통해 점수 산정 및 스탯 계산
  public UserStat calculateUserStat(UUID userId, PeriodType periodType, LocalDateTime aggregatedAt){
    // 집계 날짜를 기준으로 집계 시작 일자와 집계 끝 일자를 구한다.
    LocalDateTime periodStart = periodType.calculateStart(aggregatedAt);
    LocalDateTime periodEnd = periodType.calculateEnd(aggregatedAt);

    // 집계 날짜 범위 안에 작성한 댓글의 개수를 가져온다.
    long commentCount = commentRepository.countByUserIdAndPeriod(userId, periodStart, periodEnd);

    // 집계 날짜 범위 안에 작성한 리뷰들을 추출하고, 점수를 sum up.
    double reviewScoreSum = reviewRepository.sumRatingByUserIdAndPeriod(userId, periodStart, periodEnd);

    // 집계 날짜 범위 안에 누른 좋아요의 개수를 구한다.
    long likeCount = likeRepository.countByUserIdAndPeriod(userId, periodStart, periodEnd);

    double score = (reviewScoreSum * REVIEW_SCORE_WEIGHT) + (commentCount * COMMENT_COUNT_WEIGHT)
        + (likeCount * LIKE_COUNT_WEIGHT);

    return new UserStat(
        userId,
        reviewScoreSum,
        likeCount,
        commentCount,
        score
    );
  }

  // 오름차순으로 정렬 된 PowerUser 리스트에 0L으로 초기화된 RANK를 순서대로 부여하는 메서드
  @Transactional
  public void rankPowerUsers(PeriodType periodType, LocalDateTime aggregatedAt, UUID snapshotId){

    // 집계 일자를 기준으로 시작, 끝 날짜를 구함.
    LocalDateTime periodStart = periodType.calculateStart(aggregatedAt);
    LocalDateTime periodEnd= periodType.calculateEnd(aggregatedAt);

    // 집계 일자 범위 내에 해당되고 새로 생성한 snapshot에 해당하는 PowerUser를 list로 추출
    List<PowerUser> powers = powerUserRepository.findByPeriodDescByScore(periodType, periodStart, periodEnd, snapshotId);
    // 첫 번째 순서(score가 가장 높은 파워 유저) 랭크
    long rank = 1L;
    // 이전 점수는 처음에는 NaN으로 초기화
    double previousScore = NaN;
    // powers를 순회하면서 인덱스를 나타낼 index 변수
    long index = 1L;

    // powers 리스트를 순회
    for(PowerUser powerUser : powers){
      // 인덱스가 1 (처음 순서)거나, 해당 인덱스의 파워유저 스코어가 이전과 다르면
      if(index == 1L || Double.compare(powerUser.getScore(), previousScore) != 0){
        rank = index;
        previousScore = powerUser.getScore();
      }
      powerUser.updateRank(rank); // 랭크 부여
      index++; // 인덱스 넘김
    }
  }


  // 유저를 파워 유저로 변환하는 메서드 (랭크는 빈값으로 둠)
  public PowerUser toPowerUser(User user, PeriodType periodType, LocalDateTime aggregatedAt, UUID snapshotId){
    UserStat stat = calculateUserStat(user.getId(), periodType, aggregatedAt);

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

}
