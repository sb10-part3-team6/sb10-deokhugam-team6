package com.codeit.mission.deokhugam.user.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.codeit.mission.deokhugam.comment.repository.CommentRepository;
import com.codeit.mission.deokhugam.notification.repository.NotificationRepository;
import com.codeit.mission.deokhugam.review.repository.ReviewRepository;
import com.codeit.mission.deokhugam.user.repository.UserRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

@ExtendWith(MockitoExtension.class)
class UserHardDeleteJobConfigTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private ReviewRepository reviewRepository;

  @Mock
  private CommentRepository commentRepository;

  @Mock
  private NotificationRepository notificationRepository;

  private UserHardDeleteJobConfig config;

  @BeforeEach
  void setUp() {
    config = new UserHardDeleteJobConfig(
        userRepository, reviewRepository, commentRepository, notificationRepository
    );
  }

  @Test
  @DisplayName("Writer: 빈 리스트가 들어오면 삭제 관련 메서드를 호출하지 않는지 확인")
  void writer_DoesNotCallDelete_WhenEmptyList() throws Exception {
    // given
    ItemWriter<UUID> writer = buildWriter(Instant.now().minus(1, ChronoUnit.DAYS));

    // when
    writer.write(new Chunk<>(List.of()));

    // then - 아무 삭제 메서드도 호출되지 않아야 함
    verify(reviewRepository, never()).deleteLikesByReviewUserIds(anyList());
    verify(commentRepository, never()).deleteByReviewUserIds(anyList());
    verify(notificationRepository, never()).deleteByReviewUserIds(anyList());
    verify(reviewRepository, never()).deleteLikesByUserIds(anyList());
    verify(commentRepository, never()).deleteByUserIds(anyList());
    verify(notificationRepository, never()).deleteByUserIds(anyList());
    verify(reviewRepository, never()).deleteByUserIds(anyList());
    verify(userRepository, never()).hardDeleteByIds(anyList(), any());
  }

  @Test
  @DisplayName("Writer: 삭제 순서가 자식 → 부모 순으로 실행되는지 확인 (참조 무결성)")
  void writer_DeletesInCorrectOrder() throws Exception {
    // given
    UUID userId = UUID.randomUUID();
    ItemWriter<UUID> writer = buildWriter(Instant.now().minus(1, ChronoUnit.DAYS));

    // when
    writer.write(new Chunk<>(List.of(userId)));

    // then - 순서 검증: 자식의 자식 → 자식 → 유저 본인
    InOrder order = inOrder(reviewRepository, commentRepository, notificationRepository,
        userRepository);

    // 1단계: 리뷰에 달린 연관 데이터 삭제 (자식의 자식)
    order.verify(reviewRepository).deleteLikesByReviewUserIds(anyList());
    order.verify(commentRepository).deleteByReviewUserIds(anyList());
    order.verify(notificationRepository).deleteByReviewUserIds(anyList());

    // 2단계: 유저 본인 활동 데이터 삭제
    order.verify(reviewRepository).deleteLikesByUserIds(anyList());
    order.verify(commentRepository).deleteByUserIds(anyList());
    order.verify(notificationRepository).deleteByUserIds(anyList());
    order.verify(reviewRepository).deleteByUserIds(anyList());

    // 3단계: 유저 물리 삭제
    order.verify(userRepository).hardDeleteByIds(anyList(), any(Instant.class));
  }

  @Test
  @DisplayName("Writer: hardDeleteByIds에 올바른 userId 목록과 threshold가 전달되는지 확인")
  void writer_PassesCorrectUserIdsAndThreshold() throws Exception {
    // given
    UUID userId1 = UUID.randomUUID();
    UUID userId2 = UUID.randomUUID();
    Instant threshold = Instant.now().minus(2, ChronoUnit.DAYS);
    ItemWriter<UUID> writer = buildWriter(threshold);

    // when
    writer.write(new Chunk<>(List.of(userId1, userId2)));

    // then
    verify(userRepository).hardDeleteByIds(List.of(userId1, userId2), threshold);
  }

  @Test
  @DisplayName("Writer: 단일 사용자 ID로 모든 연관 데이터 삭제 메서드가 각 1회 호출되는지 확인")
  void writer_CallsAllDeleteMethods_Once_ForSingleUser() throws Exception {
    // given
    UUID userId = UUID.randomUUID();
    ItemWriter<UUID> writer = buildWriter(Instant.now().minus(1, ChronoUnit.DAYS));

    // when
    writer.write(new Chunk<>(List.of(userId)));

    // then - 각 메서드가 정확히 1회 호출
    verify(reviewRepository).deleteLikesByReviewUserIds(List.of(userId));
    verify(commentRepository).deleteByReviewUserIds(List.of(userId));
    verify(notificationRepository).deleteByReviewUserIds(List.of(userId));
    verify(reviewRepository).deleteLikesByUserIds(List.of(userId));
    verify(commentRepository).deleteByUserIds(List.of(userId));
    verify(notificationRepository).deleteByUserIds(List.of(userId));
    verify(reviewRepository).deleteByUserIds(List.of(userId));
    verify(userRepository).hardDeleteByIds(eq(List.of(userId)), any(Instant.class));
  }

  @Test
  @DisplayName("Listener: beforeJob에서 설정한 threshold가 현재 시각 기준 1일 이전인지 확인")
  void listener_ThresholdIsOneDayBeforeNow() {
    // given
    org.springframework.batch.core.JobExecution jobExecution =
        new org.springframework.batch.core.JobExecution(1L);

    // when
    config.userHardDeleteJobListener().beforeJob(jobExecution);

    // then
    String thresholdStr = jobExecution.getExecutionContext().getString("threshold");
    Instant threshold = Instant.parse(thresholdStr);

    Instant expectedThreshold = Instant.now().minus(1, ChronoUnit.DAYS);
    // 1일 이전 시각 근방(±5초)인지 확인
    assertThat(threshold).isBetween(
        expectedThreshold.minusSeconds(5),
        expectedThreshold.plusSeconds(5)
    );
  }


  private ItemWriter<UUID> buildWriter(Instant threshold) {
    return config.userHardDeleteWriter(threshold.toString());
  }
}
