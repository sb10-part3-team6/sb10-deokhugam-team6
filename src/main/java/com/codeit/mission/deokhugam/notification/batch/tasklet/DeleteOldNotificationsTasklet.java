package com.codeit.mission.deokhugam.notification.batch.tasklet;

import com.codeit.mission.deokhugam.notification.repository.NotificationRepository;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

// step 시작 시 실행되는 작업 로직 정의
@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteOldNotificationsTasklet implements Tasklet {

  private final NotificationRepository notificationRepository;

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
    // 삭제 날짜 컷오프 설정
    Instant cutoff = Instant.now().minus(Duration.ofDays(7));

    // 확인한지 1주일이 지난 알림 데이터 삭제
    int deletedCount = notificationRepository.deleteByConfirmedTrueAndUpdatedAtBefore(cutoff);
    
    log.info("[DELETE_OLD_NOTIFICATIONS_TASKLET] Deleted {} old notifications", deletedCount);
    contribution.incrementWriteCount(deletedCount);

    // 스텝 종료
    return RepeatStatus.FINISHED;
  }
}
