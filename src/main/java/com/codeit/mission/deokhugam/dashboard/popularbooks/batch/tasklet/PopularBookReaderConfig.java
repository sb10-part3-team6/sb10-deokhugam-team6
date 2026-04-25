package com.codeit.mission.deokhugam.dashboard.popularbooks.batch.tasklet;

import com.codeit.mission.deokhugam.book.entity.Book;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@StepScope
public class PopularBookReaderConfig {

  @Bean
  JpaPagingItemReader<Book> bookReader(EntityManagerFactory emf){
    JpaPagingItemReader<Book> reader = new JpaPagingItemReader<>();
    reader.setName("bookReader");
    reader.setEntityManagerFactory(emf);
    reader.setQueryString("select b from Book b order by b.id");
    reader.setPageSize(100);
    return reader;
  }

}
