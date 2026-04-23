package com.codeit.mission.deokhugam.dashboard.users.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.codeit.mission.deokhugam.comment.repository.CommentRepository;
import com.codeit.mission.deokhugam.dashboard.PeriodType;
import com.codeit.mission.deokhugam.dashboard.powerusers.dto.PowerUserLikeCount;
import com.codeit.mission.deokhugam.dashboard.powerusers.dto.UserCommentCount;
import com.codeit.mission.deokhugam.dashboard.powerusers.dto.UserReviewAggregate;
import com.codeit.mission.deokhugam.dashboard.powerusers.dto.UserStat;
import com.codeit.mission.deokhugam.dashboard.powerusers.entity.PowerUser;
import com.codeit.mission.deokhugam.dashboard.powerusers.repository.PowerUserRepository;
import com.codeit.mission.deokhugam.dashboard.powerusers.service.PowerUserAggregateService;
import com.codeit.mission.deokhugam.review.entity.ReviewStatus;
import com.codeit.mission.deokhugam.review.repository.ReviewLikeRepository;
import com.codeit.mission.deokhugam.review.repository.ReviewRepository;
import com.codeit.mission.deokhugam.user.entity.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PowerUserAggregateSnapshotServiceTest {

  @Mock
  private CommentRepository commentRepository;

  @Mock
  private ReviewLikeRepository reviewLikeRepository;

  @Mock
  private ReviewRepository reviewRepository;

  @Mock
  private PowerUserRepository powerUserRepository;

  @Mock
  private User user;

  @InjectMocks
  private PowerUserAggregateService powerUserAggregateService;

  @Test
  @DisplayName("기간 내 활동을 userId 기준으로 벌크 집계한다")
  void loadUserStatsAggregatesByUserId() {
    // given
    // 집계 날짜 (2026년 4월 20일 0시 0분)
    LocalDateTime aggregatedAt = LocalDateTime.of(2026, 4, 20, 0, 0);
    LocalDateTime periodStart = aggregatedAt.minusDays(1); // 집계 기준 시작일(2026년 4월 19일 0시 0분)

    UUID userId1 = UUID.randomUUID();
    UUID userId2 = UUID.randomUUID();

    // 집계 기간 내 유저 별 댓글 개수를 가져오는 메서드 호출 시,
    // user1은 댓글 두개, user2는 댓글 한개
    when(commentRepository.findUserCommentCounts(periodStart, aggregatedAt)).thenReturn(
        List.of(
            new UserCommentCount(userId1, 2L),
            new UserCommentCount(userId2, 1L)
        ));
    // 기간 내 리뷰 점수의 합을 구하는 메서드 호출 시
    // user1은 10.0점
    when(reviewRepository.findUserReviewAggregates(periodStart, aggregatedAt, ReviewStatus.ACTIVE))
        .thenReturn(List.of(new UserReviewAggregate(userId1, 10.0d)));

    // 누른 좋아요 수를 구하는 메서드 호출 시
    // user2는 3개
    when(reviewLikeRepository.findUserLikeCounts(periodStart, aggregatedAt)).thenReturn(
        List.of(new PowerUserLikeCount(userId2, 3L)));

    // when
    // 유저의 활동 점수를 집계하기 위해 스탯을 로드하는 메서드 호출
    Map<UUID, UserStat> result =
        powerUserAggregateService.loadUserStats(PeriodType.DAILY, aggregatedAt);

    // then
    assertEquals(2, result.size()); // 두 명의 유저 -> 사이즈도 두개

    // 유저 1 스탯
    assertEquals(10.0d, result.get(userId1).reviewScoreSum(), 0.0001d); // 유저 1의 리뷰 합계 -> 10.0
    assertEquals(0L, result.get(userId1).likeCount());
    assertEquals(2L, result.get(userId1).commentCount());
    assertEquals(5.6d, result.get(userId1).score(), 0.0001d);

    // 유저 2 스탯
    assertEquals(0.0d, result.get(userId2).reviewScoreSum(), 0.0001d);
    assertEquals(3L, result.get(userId2).likeCount());
    assertEquals(1L, result.get(userId2).commentCount());
    assertEquals(0.9d, result.get(userId2).score(), 0.0001d);
  }

  @Test
  @DisplayName("PowerUser 변환 시 미리 계산된 스탯만 사용한다")
  void toPowerUserUsesPrecomputedStat() {
    // given
    UUID userId = UUID.randomUUID();
    LocalDateTime aggregatedAt = LocalDateTime.of(2026, 4, 20, 0, 0);
    UserStat stat = new UserStat(userId, 12.0d, 4L, 3L, 7.7d);

    when(user.getId()).thenReturn(userId);

    // when
    PowerUser powerUser = powerUserAggregateService.toPowerUser(
        user,
        stat,
        PeriodType.WEEKLY,
        aggregatedAt,
        UUID.randomUUID());

    // then
    assertNotNull(powerUser);
    assertEquals(userId, powerUser.getUserId());
    assertEquals(12.0d, powerUser.getReviewScoreSum(), 0.0001d);
    assertEquals(4L, powerUser.getLikeCount());
    assertEquals(3L, powerUser.getCommentCount());
    assertEquals(7.7d, powerUser.getScore(), 0.0001d);
    verifyNoInteractions(commentRepository, reviewRepository, powerUserRepository);
  }
}
