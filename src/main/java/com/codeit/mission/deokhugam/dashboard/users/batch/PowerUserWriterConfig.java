package com.codeit.mission.deokhugam.dashboard.users.batch;

import com.codeit.mission.deokhugam.dashboard.users.entity.PowerUser;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PowerUserWriterConfig {

  // PowerUser를 쓰는 ItemWriter
  @Bean
  public JpaItemWriter<PowerUser> powerUserItemWriter(EntityManagerFactory emf){
    JpaItemWriter<PowerUser> writer = new JpaItemWriter<>(); // JpaItemWriter 객체 생성
    writer.setEntityManagerFactory(emf); // 해당 객체에 EntityMangerFactory를 주입한다.
    return writer; // 객체 반환
  }
}
