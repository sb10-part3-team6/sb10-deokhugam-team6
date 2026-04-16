package com.codeit.mission.deokhugam.dashboard.users;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class PowerUserJobConfig {

  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;
  private final PowerUserJobTasklet powerUserJobTasklet;

  @Bean
  public Step aggregatePowerUserStep() {
    return new StepBuilder("aggregatePowerUserStep", jobRepository)
        .tasklet(powerUserJobTasklet, transactionManager)
        .build();
  }

  @Bean
  public Job aggregatePowerUserJob() {
    return new JobBuilder("aggregatePowerUserJob", jobRepository)
        .start(aggregatePowerUserStep())
        .build();
  }
}
