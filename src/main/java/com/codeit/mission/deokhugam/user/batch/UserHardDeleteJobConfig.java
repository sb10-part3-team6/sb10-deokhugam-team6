package com.codeit.mission.deokhugam.user.batch;

import com.codeit.mission.deokhugam.comment.repository.CommentRepository;
import com.codeit.mission.deokhugam.notification.repository.NotificationRepository;
import com.codeit.mission.deokhugam.review.repository.ReviewRepository;
import com.codeit.mission.deokhugam.user.repository.UserRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

// 탈퇴 유저 물리 삭제를 위한 Spring Batch 설정
// 논리 삭제 후 1일이 경과한 유저와 연관 데이터를 영구 삭제함
@Slf4j
@Configuration
@RequiredArgsConstructor
public class UserHardDeleteJobConfig {

  private final UserRepository userRepository;
  private final ReviewRepository reviewRepository;
  private final CommentRepository commentRepository;
  private final NotificationRepository notificationRepository;

  private static final int CHUNK_SIZE = 100; // 한 번에 처리할 유저 수

  @Bean
  public Job userHardDeleteJob(JobRepository jobRepository, Step userHardDeleteStep) {
    return new JobBuilder("userHardDeleteJob", jobRepository)
        .start(userHardDeleteStep)
        .listener(userHardDeleteJobListener())
        .build();
  }

  @Bean
  public JobExecutionListener userHardDeleteJobListener() {
    return new JobExecutionListener() {
      @Override
      public void beforeJob(JobExecution jobExecution) {
        Instant threshold = Instant.now().minus(1, ChronoUnit.DAYS);
        jobExecution.getExecutionContext().put("threshold", threshold.toString());
        log.info("[Batch] 탈퇴 유저 물리 삭제 기준 시간 설정: {}", threshold);
      }
    };
  }

  @Bean
  public Step userHardDeleteStep(JobRepository jobRepository,
      PlatformTransactionManager transactionManager,
      RepositoryItemReader<UUID> userHardDeleteReader,
      ItemWriter<UUID> userHardDeleteWriter) {
    return new StepBuilder("userHardDeleteStep", jobRepository)
        .<UUID, UUID>chunk(CHUNK_SIZE, transactionManager)
        .reader(userHardDeleteReader)
        .writer(userHardDeleteWriter)
        .build();
  }

  // 탈퇴한 지 1일이 지난 유저 ID들을 읽어오는 Reader
  // RepositoryItemReader를 사용하여 페이지 단위로 읽어옴 (OOM 방지)
  // @StepScope: JobExecutionListener가 설정한 threshold 값을 읽어오기 위해 필요함
  @Bean
  @StepScope
  public RepositoryItemReader<UUID> userHardDeleteReader(
      @Value("#{jobExecutionContext['threshold']}") String thresholdStr) {
    Instant threshold = Instant.parse(thresholdStr);

    return new RepositoryItemReaderBuilder<UUID>()
        .name("userHardDeleteReader")
        .repository(userRepository)
        .methodName("findDeletedUserIdsOlderThan")
        .arguments(Collections.singletonList(threshold))
        .pageSize(CHUNK_SIZE)
        .sorts(Collections.singletonMap("id", Sort.Direction.ASC)) // 페이징을 위한 정렬 설정
        .build();
  }

  // 읽어온 유저 ID들을 기반으로 모든 연관 데이터를 정합성에 맞게 삭제하는 Writer
  // 빈 리스트 전달 시 SQL 에러 방지를 위해 호출 전 유효성 체크 수행
  // @StepScope: JobExecutionListener가 설정한 threshold 값을 읽어오기 위해 필요함
  @Bean
  @StepScope
  public ItemWriter<UUID> userHardDeleteWriter(
      @Value("#{jobExecutionContext['threshold']}") String thresholdStr) {
    Instant threshold = Instant.parse(thresholdStr);

    return chunk -> {
      List<UUID> userIds = (List<UUID>) chunk.getItems();
      if (userIds.isEmpty()) {
        return;
      }
      log.info("[Batch] {}명의 유저 및 연관 데이터 물리 삭제 시작", userIds.size());

      // 1. 삭제 대상 유저들이 작성한 리뷰에 달린 연관 데이터 먼저 삭제 (자식의 자식)
      // 서브쿼리를 사용하여 reviewIds를 메모리에 올리지 않고 직접 삭제함
      reviewRepository.deleteLikesByReviewUserIds(userIds);
      commentRepository.deleteByReviewUserIds(userIds);
      notificationRepository.deleteByReviewUserIds(userIds);

      // 2. 유저 본인의 활동 데이터 삭제
      reviewRepository.deleteLikesByUserIds(userIds);
      commentRepository.deleteByUserIds(userIds);
      notificationRepository.deleteByUserIds(userIds);
      reviewRepository.deleteByUserIds(userIds);

      // 3. 최종 유저 본인 삭제 (물리 삭제)
      // TOCTOU 방지를 위해 상태와 Listener에서 전달받은 시간을 다시 한번 검증
      userRepository.hardDeleteByIds(userIds, threshold);

      log.info("[Batch] {}명의 물리 삭제가 완벽하게 완료되었습니다.", userIds.size());
    };
  }
}
