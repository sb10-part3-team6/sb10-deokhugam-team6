package com.codeit.mission.deokhugam.dashboard.popularreviews.batch;

import com.codeit.mission.deokhugam.review.entity.Review;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PopularReviewReaderConfig {

  @Bean
  JpaPagingItemReader<Review> reviewReader(EntityManagerFactory emf){
    JpaPagingItemReader<Review> reader = new JpaPagingItemReader<>();
    reader.setName("reviewReader");
    reader.setEntityManagerFactory(emf);
    reader.setQueryString("select r from Review r order by r.id");
    reader.setPageSize(100);
    return reader;
  }




}
