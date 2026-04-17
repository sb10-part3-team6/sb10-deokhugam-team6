package com.codeit.mission.deokhugam.dashboard.users.batch;

import com.codeit.mission.deokhugam.dashboard.PeriodType;
import com.codeit.mission.deokhugam.dashboard.users.repository.PowerUserRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@StepScope
// 오래된 파워 유저들을 정리하는 Tasklet
public class DeleteOldPowerUsersTasklet implements Tasklet {
  private final PowerUserRepository powerUserRepository;

  // 외부로부터 PeriodType 과 집계 날짜를 가져온다.
  @Value("#{jobParameters['periodType']}") String periodTypeValue;
  @Value("#{jobParameters['aggregatedAt']}") String aggregatedAtValue;


  @Override
  public @Nullable RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
      throws Exception {

    // String으로 들어온 정보들은 Parsing
    LocalDateTime aggregatedAt = LocalDateTime.parse(aggregatedAtValue);
    PeriodType periodType = PeriodType.valueOf(periodTypeValue);

    // 집계 범위를 위한 시작/끝 날짜 구함
    LocalDateTime periodStart = periodType.calculateStart(aggregatedAt);
    LocalDateTime periodEnd = periodType.calculateEnd(aggregatedAt);

    // 특정 기간의 파워 유저들을 지우는 레포지토리 메서드 수행
    powerUserRepository.deleteByPeriodTypeAndPeriodStartAndPeriodEnd(periodType, periodStart, periodEnd);

    // 작업이 끝났음을 알림.
    return RepeatStatus.FINISHED;
  }
}
