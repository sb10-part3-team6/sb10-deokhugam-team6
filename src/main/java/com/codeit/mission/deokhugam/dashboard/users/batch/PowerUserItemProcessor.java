package com.codeit.mission.deokhugam.dashboard.users.batch;

import com.codeit.mission.deokhugam.dashboard.PeriodType;
import com.codeit.mission.deokhugam.dashboard.users.dto.UserStat;
import com.codeit.mission.deokhugam.dashboard.users.entity.PowerUser;
import com.codeit.mission.deokhugam.dashboard.users.service.PowerUserAggregateService;
import com.codeit.mission.deokhugam.user.entity.User;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@StepScope
public class PowerUserItemProcessor implements ItemProcessor<User, PowerUser> {
  private final PowerUserAggregateService powerUserAggregateService;

  private PeriodType periodType;
  private LocalDateTime aggregatedAt;
  private UUID snapshotId;
  private Map<UUID, UserStat> statsByUserId = Map.of();

  // Step 시작 전 매개변수들을 초기화한다.
  // 현재 실행 객체의 context를 주입받아 변수를 초기화한다.
  @BeforeStep
  void beforeStep(StepExecution stepExecution){
    // 이전 Step에서 주입받아 컨텍스트에 저장된 Job Parameter 들을 이렇게 뽑아올 수 있다고 한다.
    this.periodType = PeriodType.valueOf(stepExecution.getJobExecution().getJobParameters().getString("periodType"));
    this.aggregatedAt = LocalDateTime.parse(stepExecution.getJobExecution().getJobParameters().getString("aggregatedAt"));
    this.snapshotId = UUID.fromString(stepExecution.getJobExecution().getExecutionContext().getString("snapshotId"));
    this.statsByUserId = powerUserAggregateService.loadUserStats(periodType, aggregatedAt); // Aggregate 서비스에서 유저 스탯을 로드하는 메서드 호출
  }

  @Override
  public @Nullable PowerUser process(@NonNull User item) throws Exception {
    // User id 별 UserStat을 이전 itemReader이 반환한 User로부터 가져온다.
    UserStat stat =
        statsByUserId.getOrDefault(item.getId(), powerUserAggregateService.emptyStat(item.getId()));
    return powerUserAggregateService.toPowerUser(item, stat, periodType, aggregatedAt, snapshotId);
  }
}
