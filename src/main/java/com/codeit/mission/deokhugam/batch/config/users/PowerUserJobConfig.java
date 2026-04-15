package com.codeit.mission.deokhugam.batch.config.users;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.TransactionManager;

@Configuration
@RequiredArgsConstructor
public class PowerUserJobConfig {

  private final JobRepository jobRepository;
  private final TransactionManager transactionManager;

  // 활동 점수 = (해당 기간의 작성한 리뷰의 인기 점수 * 0.5) + (참여한 좋아요 수 * 0.2) + (참여한 댓글 수 * 0.3)
  /*

   */
}
