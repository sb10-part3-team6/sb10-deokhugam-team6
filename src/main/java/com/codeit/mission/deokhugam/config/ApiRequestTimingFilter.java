package com.codeit.mission.deokhugam.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@Order(Integer.MIN_VALUE) // 체인에서 가장 앞 위치
public class ApiRequestTimingFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain
  ) throws ServletException, IOException {
    long start = System.nanoTime();

    try{
      filterChain.doFilter(request, response);
    } finally {
      long elapsedMs = (System.nanoTime() - start) / 1_000_000;

      if(request.getRequestURI().startsWith("/api")) {
        log.info("[API_TIMING] method={}, uri={}, query={}, status={}, elapsedMs={}",
            request.getMethod(),
            request.getRequestURI(),
            request.getQueryString(),
            response.getStatus(),
            elapsedMs
            );
      }
    }
  }
}
