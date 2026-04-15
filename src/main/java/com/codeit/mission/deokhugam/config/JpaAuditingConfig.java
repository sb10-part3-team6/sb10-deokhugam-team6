package com.codeit.mission.deokhugam.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/*
    JPA Auditing 설정
    ----------------
    엔티티의 생성 시점과 수정 시점을 자동으로 기록하기 위해 활성화
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}