package com.codeit.mission.deokhugam.dashboard.users.entity;

import com.codeit.mission.deokhugam.base.BaseEntity;
import com.codeit.mission.deokhugam.dashboard.PeriodType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table( // 추후 schema.sql 등을 통해 테이블이 인식되도록 해야 할 듯.
    name = "power_users",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_power_users_period",
          columnNames = {"user_id", "period_type", "period_start", "period_end"})
    })
public class PowerUser extends BaseEntity {

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Enumerated(EnumType.STRING)
  @Column(name = "period_type", nullable = false, length = 20)
  private PeriodType periodType;

  @Column(name = "period_start", nullable = false)
  private LocalDateTime periodStart;

  @Column(name = "period_end", nullable = false)
  private LocalDateTime periodEnd;

  @Column(nullable = false)
  private long rank;

  @Column(nullable = false)
  private double score;

  @Column(name = "review_score_sum", nullable = false)
  private double reviewScoreSum;

  @Column(name = "like_count", nullable = false)
  private long likeCount;

  @Column(name = "comment_count", nullable = false)
  private long commentCount;

  @Column(name = "aggregated_at", nullable = false)
  private LocalDateTime aggregatedAt;

  @Builder
  public PowerUser(
      UUID userId,
      PeriodType periodType,
      LocalDateTime periodStart,
      LocalDateTime periodEnd,
      long rank,
      double score,
      double reviewScoreSum,
      long likeCount,
      long commentCount,
      LocalDateTime aggregatedAt) {
    this.userId = userId;
    this.periodType = periodType;
    this.periodStart = periodStart;
    this.periodEnd = periodEnd;
    this.rank = rank;
    this.score = score;
    this.reviewScoreSum = reviewScoreSum;
    this.likeCount = likeCount;
    this.commentCount = commentCount;
    this.aggregatedAt = aggregatedAt;
  }
}
