package com.codeit.mission.deokhugam.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Duration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching // 캐시 애노테이션을 활성화
public class RedisConfig {

  // Spring Cache가 Redis를 캐시 저장소로 쓰게 해주는 관리자
  @Bean
  public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory){
    ObjectMapper objectMapper = new ObjectMapper() // 직렬화를 하는 오브젝트 매퍼
        .registerModule(new JavaTimeModule()) // Instant, LocalDateTime, LocalDate 같은 Java time 타입 직렬화 지원
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // 날자를 Timestamps로 쓰지 않기 (문자열 형태로 날짜를 저장)

    // 캐시에서 JSON을 다시 JAVA 객체로 복원할 때 타입 정보를 포함시키는 설정
    objectMapper.activateDefaultTyping( // JSON으로 직렬화할 때, 자바 객체의 타입 정보도 포함하라는 뜻
        BasicPolymorphicTypeValidator.builder() // 아무 객체 타입이나 들어가면 안되니, Validator로 검증함.
            .allowIfSubType("com.codeit.mission.deokhugam") // 해당 프로젝트 패키지 아래 클래스만 허용
            .allowIfSubType("java.util") // List,Map 등의 컬렉션 타입을 허용
            .build(),
        ObjectMapper.DefaultTyping.NON_FINAL, // final이 아닌 타입에는 타입 정보를 붙인다
        JsonTypeInfo.As.PROPERTY // 타입 정보를 JSON 안의 속성으로 넣는다
    );

    // Redis에 저장될 value를 JSON 형태로 직렬화/역직렬화하기 위한 Serializer 설정
    // ObjectMapper에 JavaTimeModule, 타입 정보 설정 등이 들어가 있으므로
    // LocalDateTime 같은 시간 타입이나 DTO 타입 복원도 처리할 수 있음
    RedisSerializer<Object> valueSerializer =
        new GenericJackson2JsonRedisSerializer(objectMapper);

    // Redis Cache의 기본 설정을 정의
    RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()

        // 기본 캐시 유지 시간을 10분으로 설정
        .entryTtl(Duration.ofMinutes(10))

        // null 값은 캐싱하지 않도록 설정
        .disableCachingNullValues()

        // Redis key를 문자열 형태로 저장
        // ex) deokhugam:popularBooks:period=DAILY:direction=DESC...
        // StringRedisSerializer를 사용하여 Redis CLI에서 사람이 이해하기 쉽게끔 함
        .serializeKeysWith(
            RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
        )

        // Redis value를 JSON 형태로 저장
        // Java 객체를 Redis에 저장할 때 JSON으로 변환
        // Redis에서 꺼낼 때 Java 객체로 변환
        .serializeValuesWith(
            RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer)
        )

        // 캐시 key 앞에 붙을 prefix 설정
        .computePrefixWith(cacheName -> "deokhugam:" + cacheName + ":");

    // RedisCacheManager 생성
    return RedisCacheManager.builder(connectionFactory)

        // 위에서 만든 기본 캐시 설정을 전체 캐시에 적용
        .cacheDefaults(defaultConfig)

        // 인기 도서 캐시는 기본 설정을 사용하되 ttl=30
        .withCacheConfiguration(
            "popularBooks",
            defaultConfig.entryTtl(Duration.ofMinutes(30))
        )

        // 인기 리뷰도 동일
        .withCacheConfiguration(
            "popularReviews",
            defaultConfig.entryTtl(Duration.ofMinutes(30))
        )

        // 파워 유저도 동일
        .withCacheConfiguration(
            "powerUsers",
            defaultConfig.entryTtl(Duration.ofMinutes(30))
        )

        // RedisCacheManager 객체 생성
        .build();




  }
}
