package com.codeit.mission.deokhugam.dashboard.users.batch.tasklet;

import com.codeit.mission.deokhugam.dashboard.PeriodType;
import com.codeit.mission.deokhugam.dashboard.users.entity.PowerUser;
import com.codeit.mission.deokhugam.dashboard.users.entity.PowerUserSnapshot;
import com.codeit.mission.deokhugam.dashboard.users.service.PowerUserSnapshotService;
import java.time.LocalDateTime;
import java.util.UUID;
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
@StepScope
@RequiredArgsConstructor
public class CreateNewSnapshotTasklet implements Tasklet {
  private final PowerUserSnapshotService powerUserSnapshotService;

  // 외부로부터 변수를 받아온다.
  @Value("#{jobParameters['periodType']}")
  private String periodTypeValue;

  @Value("#{jobParameters['aggregatedAt']}")
  private String aggregatedAtValue;

  @Override
  public @Nullable RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {

    // 파싱
    PeriodType periodType = PeriodType.valueOf(periodTypeValue);
    LocalDateTime aggregatedAt = LocalDateTime.parse(aggregatedAtValue);

    // 스냅샷 객체 생성 서비스 메서드를 호출함.
    PowerUserSnapshot ps = powerUserSnapshotService.createStagingSnapshot(periodType,aggregatedAt);

    // 다음 스텝인 AggregateStep에 스냅샷 Id를 전달하기 위해 jobExecutionContext에 값을 저장함.
    chunkContext.getStepContext() // 현재 실행 중인 Step의 컨텍스트 정보 가져옴
        .getStepExecution() // 현재 Step 실행 객체
        .getJobExecution() // 이 속한 전체 Job 실행 객체
        .getExecutionContext() // Job 전체가 공유하는 Key-Value 저장소
        .putString("snapshotId", ps.getSnapshotId().toString()); // 에 snapshotId 이름으로 해당 Id를 저장

    // 스텝 종료를 알림
    return RepeatStatus.FINISHED;
  }
}
