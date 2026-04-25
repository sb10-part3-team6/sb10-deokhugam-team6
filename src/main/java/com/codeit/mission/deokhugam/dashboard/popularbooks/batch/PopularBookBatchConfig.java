package com.codeit.mission.deokhugam.dashboard.popularbooks.batch;

import com.codeit.mission.deokhugam.book.entity.Book;
import com.codeit.mission.deokhugam.dashboard.batch.DashboardAggregationJobListener;
import com.codeit.mission.deokhugam.dashboard.popularbooks.batch.tasklet.PopularBookItemProcessor;
import com.codeit.mission.deokhugam.dashboard.popularbooks.batch.tasklet.RankPopularBookTasklet;
import com.codeit.mission.deokhugam.dashboard.popularbooks.entity.PopularBook;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class PopularBookBatchConfig {
  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;

  @Bean
  public Job popularBookAggregationJob(
      Step createNewSnapshotStep,
      Step aggregatePopularBooksStep,
      Step rankPopularBooksStep,
      Step publishSnapshotStep,
      DashboardAggregationJobListener listener){
    return new JobBuilder("popularBookAggregationJob", jobRepository)
        .listener(listener)
        .start(createNewSnapshotStep)
        .next(aggregatePopularBooksStep)
        .next(rankPopularBooksStep)
        .next(publishSnapshotStep)
        .build();
  }

  @Bean
  public Step aggregatePopularBookStep(
      @Qualifier("bookReader") JpaPagingItemReader<Book> itemReader,
      @Qualifier("bookProcessor") PopularBookItemProcessor processor,
      @Qualifier("bookWriter") JpaItemWriter<PopularBook> writer)
  {
    return new StepBuilder("aggregatePopularBookStep",jobRepository)
        .<Book, PopularBook>chunk(100, transactionManager)
        .reader(itemReader)
        .processor(processor)
        .writer(writer)
        .build();
  }

  @Bean
  public Step rankPopularBookStep(RankPopularBookTasklet rankPopularBookTasklet){
    return new StepBuilder("rankPopularBookStep",jobRepository)
        .tasklet(rankPopularBookTasklet, transactionManager)
        .build();
  }

}
