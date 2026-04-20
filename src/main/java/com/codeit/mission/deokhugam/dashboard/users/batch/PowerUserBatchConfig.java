package com.codeit.mission.deokhugam.dashboard.users.batch;

import com.codeit.mission.deokhugam.dashboard.users.batch.tasklet.CreateNewSnapshotTasklet;
import com.codeit.mission.deokhugam.dashboard.users.batch.tasklet.PublishSnapshotTasklet;
import com.codeit.mission.deokhugam.dashboard.users.batch.tasklet.RankPowerUsersTasklet;
import com.codeit.mission.deokhugam.dashboard.users.entity.PowerUser;
import com.codeit.mission.deokhugam.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class PowerUserBatchConfig {
  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;


  // Batch의 최상단의 작업 논리인 Job을 설정한다.
  @Bean
  public Job powerUserAggregationJob(
      Step createNewSnapshotStep,
      Step aggregatePowerUsersStep,
      Step rankPowerUsersStep,
      Step publishSnapshotStep,
      PowerUserAggregationJobListener powerUserAggregationJobListener){

    return new JobBuilder("powerUserAggregationJob", jobRepository)
        .listener(powerUserAggregationJobListener) // 리스너 주입
        .start(createNewSnapshotStep) // 스냅샷 생성 스텝
        .next(aggregatePowerUsersStep) // 파워 유저를 집계하는 스텝
        .next(rankPowerUsersStep) // 집계가 마무리 된 후 랭크를 부여하는 스텝
        .next(publishSnapshotStep) // 랭크를 부여한 후, 스냅샷을 publish하는 스텝
        .build();
  }

  // 스냅샷을 생성하는 Step을 설정한다.
  @Bean
  public Step createNewSnapshotStep(CreateNewSnapshotTasklet createNewSnapshotTasklet){
    return new StepBuilder("createNewSnapshotStep", jobRepository)
        .tasklet(createNewSnapshotTasklet, transactionManager)
        .build();
  }

  // 파워 유저 집계를 수행할 Step을 설정한다.
  @Bean
  public Step aggregatePowerUsersStep(
      JpaPagingItemReader<User> userReader,
      PowerUserItemProcessor powerUserItemProcessor,
      JpaItemWriter<PowerUser> powerUserItemWriter
  ){
    return new StepBuilder("aggregatePowerUsersStep", jobRepository)
        .<User, PowerUser>chunk(100, transactionManager) // 청크 단위로 처리할 예정
        .reader(userReader)
        .processor(powerUserItemProcessor)
        .writer(powerUserItemWriter)
        .build();
  }


  // 집계가 끝난 뒤 파워 유저 테이블을 정렬 및 랭킹을 부여하는 스텝
  @Bean
  public Step rankPowerUsersStep(RankPowerUsersTasklet rankPowerUsersTasklet){
    return new StepBuilder("rankPowerUsersStep", jobRepository)
        .tasklet(rankPowerUsersTasklet, transactionManager)
        .build();
  }

  // 새로이 집계된 파워유저 스냅샷 객체를 publish로 바꾸는 스텝
  @Bean
  public Step publishSnapshotStep(PublishSnapshotTasklet publishSnapshotTasklet){
    return new StepBuilder("publishSnapshotStep", jobRepository)
        .tasklet(publishSnapshotTasklet, transactionManager)
        .build();
  }

}
