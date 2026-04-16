package com.codeit.mission.deokhugam.dashboard.reviews;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.TransactionManager;

@Configuration
@RequiredArgsConstructor
public class PopularReviewJobConfig {

  private final JobRepository jobRepository;
  private final TransactionManager transactionManager;

  // 인기 리뷰 점수 공식
  // 점수 = (해당 기간의 좋아요 수 * 0.3) + (해당 기간의 댓글 수 * 0.7)

  /*


   */

}
