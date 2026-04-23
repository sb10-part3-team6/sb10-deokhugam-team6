package com.codeit.mission.deokhugam.dashboard.powerusers.batch;

import com.codeit.mission.deokhugam.user.entity.User;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PowerUserReaderConfig {

  // User를 읽는 ItemReader
  // JpaPagingItemReader는 Jpa 기반으로 페이지네이션을 하여 끊어서 읽어들일 수 있다.
  @Bean
  @Qualifier("reviewReader")
  public JpaPagingItemReader<User> userReader(EntityManagerFactory emf){
    JpaPagingItemReader<User> reader = new JpaPagingItemReader<>(); // JpaPagingItemReader 객체 생성
    reader.setName("userReader"); // 이름 지정
    reader.setEntityManagerFactory(emf); // EntityManagerFactory 주입
    reader.setQueryString("select u from User u order by u.id"); // 실행할 쿼리 설정
    reader.setPageSize(100); // 100개 단위로 끊어서 읽어들임
    return reader; // 객체 반환

  }


}
