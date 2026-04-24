package com.codeit.mission.deokhugam.dashboard.popularreviews.batch;

import com.codeit.mission.deokhugam.dashboard.popularreviews.entity.PopularReview;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PopularReviewWriterConfig {

  @Bean
  @Qualifier("reviewWriter")
  public JpaItemWriter<PopularReview> popularReviewItemWriter(EntityManagerFactory emf){
    JpaItemWriter<PopularReview> writer = new JpaItemWriter<>();
    writer.setEntityManagerFactory(emf);
    return writer;
  }
}
