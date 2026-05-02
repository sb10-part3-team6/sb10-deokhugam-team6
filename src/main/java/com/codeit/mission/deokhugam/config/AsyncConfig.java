package com.codeit.mission.deokhugam.config;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

  @Override
  @Bean(name = "taskExecutor")
  public Executor getAsyncExecutor() {
    // 비동기 작업에서 활용할 스레드 풀
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

    executor.setCorePoolSize(10);                   // 기본 스레드 수
    executor.setMaxPoolSize(30);                    // 최대 스레드 수
    executor.setQueueCapacity(100);                 // 대기 큐 크기
    executor.setThreadNamePrefix("Async-");         // 스레드 이름 접두사 (로그 추적용)
    executor.initialize();

    return executor;
  }

  @Override
  public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
    // 비동기 에러 헨들러
    return (Throwable ex, Method method, Object... params) -> {
      System.err.println("Async Error: " + ex.getMessage());
      System.err.println("Method: " + method.getName());
    };
  }
}
