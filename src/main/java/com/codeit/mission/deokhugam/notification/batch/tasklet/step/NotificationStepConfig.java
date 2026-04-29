package com.codeit.mission.deokhugam.notification.batch.tasklet.step;

import com.codeit.mission.deokhugam.notification.batch.tasklet.DeleteOldNotificationsTasklet;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

// 알림 관련 batch 실행 흐름 정의
@Configuration
@RequiredArgsConstructor
public class NotificationStepConfig {

  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;
  private final DeleteOldNotificationsTasklet tasklet;

  // step 정의
  @Bean
  public Step deleteOldNotificationsStep() {
    return new StepBuilder("deleteOldNotificationsStep", jobRepository)
      .tasklet(tasklet, transactionManager)
      .build();
  }

  // job 정의
  @Bean
  public Job deleteOldNotificationsJob() {
    return new JobBuilder("deleteOldNotificationsJob", jobRepository)
      .start(deleteOldNotificationsStep())
      .build();
  }
}