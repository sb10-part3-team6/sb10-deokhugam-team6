package com.codeit.mission.deokhugam.notification.batch;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class NotificationStepConfigTest {

  @Autowired
  private Job deleteOldNotificationsJob;

  @Autowired
  private Step deleteOldNotificationsStep;

  @Test
  @DisplayName("Job bean 생성 확인")
  void jobBeanIsCreated() {
    assertThat(deleteOldNotificationsJob).isNotNull();
  }

  @Test
  @DisplayName("Step bean 생성 확인")
  void stepBeanIsCreated() {
    assertThat(deleteOldNotificationsStep).isNotNull();
  }
}