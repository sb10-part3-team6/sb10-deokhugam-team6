package com.codeit.mission.deokhugam.dashboard.reviews.batch;

import com.codeit.mission.deokhugam.dashboard.reviews.entity.PopularReview;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PopularReviewWriterConfig {

  @Bean
  public JpaItemWriter<PopularReview> popularReviewItemWriter(EntityManagerFactory emf){
    JpaItemWriter<PopularReview> writer = new JpaItemWriter<>();
    writer.setEntityManagerFactory(emf);
    return writer;
  }
}
