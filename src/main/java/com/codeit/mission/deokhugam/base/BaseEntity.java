package com.codeit.mission.deokhugam.base;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.sql.Timestamp;
import java.util.UUID;

/*
    공통 엔티티
 */
@Getter
@NoArgsConstructor
@MappedSuperclass                                       // 하위 클래스에도 자동 기록 적용
@EntityListeners(AuditingEntityListener.class)          // 객체 생성 / 수정 시점 자동 기록
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)     // 무작위 UUID 발급
    private UUID id;                                    // 도매안 식별자

    @CreatedDate
    @Column(updatable = false)
    private Timestamp createdAt;                        // 객체 생성 시점

    @LastModifiedDate
    private Timestamp updatedAt;                        // 객체 수정 시점
}