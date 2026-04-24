package com.codeit.mission.deokhugam.review.batch;

import com.codeit.mission.deokhugam.comment.repository.CommentRepository;
import com.codeit.mission.deokhugam.notification.repository.NotificationRepository;
import com.codeit.mission.deokhugam.review.repository.ReviewLikeRepository;
import com.codeit.mission.deokhugam.review.repository.ReviewRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ReviewBatchConfigTest {

  @Mock
  private ReviewRepository reviewRepository;

  @Mock
  private CommentRepository commentRepository;

  @Mock
  private ReviewLikeRepository reviewLikeRepository;

  @Mock
  private NotificationRepository notificationRepository;

  @InjectMocks
  private ReviewBatchConfig reviewBatchConfig;

  /*
      리뷰 배치 컨피그
   */

  @Test
  @DisplayName("Chunk 데이터(UUID 리스트)에 담긴 리뷰와 연관 데이터 삭제 성공")
  void reviewHardDeleteWriter_Success() throws Exception {
    // given
    ItemWriter<UUID> writer = reviewBatchConfig.reviewHardDeleteWriter();

    List<UUID> targetIds = List.of(UUID.randomUUID(), UUID.randomUUID());
    Chunk<UUID> chunk = new Chunk<>(targetIds);

    // when
    writer.write(chunk);

    // then
    verify(commentRepository, times(1)).deleteByReviewIdIn(targetIds);
    verify(reviewLikeRepository, times(1)).deleteByReviewIdIn(targetIds);
    verify(notificationRepository, times(1)).deleteByReviewIdIn(targetIds);
    verify(reviewRepository, times(1)).deleteAllByIdInBatch(targetIds);
  }

  @Test
  @DisplayName("Chunk가 비어 있으면 어떤 삭제도 수행하지 않음")
  void review_handler_delete_writer_empty_chunk_no_operation() throws Exception {
    // given
    ItemWriter<UUID> writer = reviewBatchConfig.reviewHardDeleteWriter();
    Chunk<UUID> chunk = new Chunk<>(List.of());

    // when
    writer.write(chunk);

    // then
    verifyNoInteractions(commentRepository, reviewLikeRepository, notificationRepository,
        reviewRepository);
  }

  @Test
  @DisplayName("Writer 실행 도중 데이터베이스 오류가 발생한 경우, 예외를 반환")
  void reviewHardDeleteWriter_Fail_DbError() throws Exception {
    // given
    ItemWriter<UUID> writer = reviewBatchConfig.reviewHardDeleteWriter();
    List<UUID> targetIds = List.of(UUID.randomUUID());
    Chunk<UUID> chunk = new Chunk<>(targetIds);

    willThrow(new RuntimeException("DataBase Timeout Error"))
        .given(reviewRepository).deleteAllByIdInBatch(targetIds);

    // when & then
    assertThatThrownBy(() -> writer.write(chunk))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("DataBase Timeout Error");

    // Repository 메서드가 호출되긴 했는지 검증
    verify(reviewRepository, times(1)).deleteAllByIdInBatch(targetIds);
  }
}