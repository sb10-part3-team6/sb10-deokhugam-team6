package com.codeit.mission.deokhugam.dashboard.books;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.TransactionManager;

@Configuration
@RequiredArgsConstructor
public class PopularBookJobConfig {

  private final JobRepository jobRepository;
  private final TransactionManager transactionManager;



  // 인기 도서를 산정하는 기준 계산
  // 점수 = (해당 기간의 리뷰수 * 0.4) + (해당 기간의 평점 평균 * 0.6)
  /*
  1. 해당 기간의 리뷰수를 구하는 작업
  2. 해당 기간의 평점 평균을 구하는 작업
  3. 두 수를 더하고 집계 테이블을 업데이트하는 작업
   */



}
