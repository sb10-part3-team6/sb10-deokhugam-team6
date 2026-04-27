package com.codeit.mission.deokhugam.dashboard.popularbooks.batch.tasklet;

import com.codeit.mission.deokhugam.dashboard.popularbooks.entity.PopularBook;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PopularBookWriterConfig {

  @Bean
  @Qualifier("bookWriter")
  public JpaItemWriter<PopularBook> popularBookJpaItemWriter(EntityManagerFactory emf){
    JpaItemWriter<PopularBook> writer = new JpaItemWriter<>();
    writer.setEntityManagerFactory(emf);
    return writer;
  }
}
