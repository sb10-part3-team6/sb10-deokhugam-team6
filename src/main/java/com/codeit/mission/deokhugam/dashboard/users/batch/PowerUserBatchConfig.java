package com.codeit.mission.deokhugam.dashboard.users.batch;

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
      Step deleteOldPowerUsersStep,
      Step aggregatePowerUsersStep,
      Step rankPowerUsersStep){

    return new JobBuilder("powerUserAggregationJob", jobRepository)
        .start(deleteOldPowerUsersStep)
        .next(aggregatePowerUsersStep)
        .next(rankPowerUsersStep)
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

  @Bean
  public Step deleteOldPowerUsersStep(
      DeleteOldPowerUsersTasklet deleteOldPowerUsersTasklet
  ){
    return new StepBuilder("deleteOldPowerUsersStep", jobRepository)
        .tasklet(deleteOldPowerUsersTasklet, transactionManager)
        .build();
  }

  @Bean
  public Step rankPowerUsersStep(RankPowerUsersTasklet rankPowerUsersTasklet){
    return new StepBuilder("rankPowerUsersStep", jobRepository)
        .tasklet(rankPowerUsersTasklet, transactionManager)
        .build();
  }

}
