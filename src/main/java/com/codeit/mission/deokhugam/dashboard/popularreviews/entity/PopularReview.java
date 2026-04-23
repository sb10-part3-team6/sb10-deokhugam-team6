package com.codeit.mission.deokhugam.dashboard.popularreviews.entity;

import com.codeit.mission.deokhugam.base.BaseEntity;
import com.codeit.mission.deokhugam.dashboard.PeriodType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
@Table(
    name = "popular_reviews",
    indexes={
        @Index(name = "idx_popular_reviews_period_start_rank", columnList = "period_type, period_start, rank"),
        @Index(name = "idx_popular_reviews_snapshot_rank", columnList = "snapshot_id, rank")
    },
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_popular_reviews_period",
            columnNames = {"review_id", "period_type", "period_start"}
        )
    })

public class PopularReview extends BaseEntity {
  @Column(name = "review_id", nullable = false)
  private UUID reviewId;

  @Enumerated(EnumType.STRING)
  @Column(name = "period_type", nullable = false, length = 20)
  private PeriodType periodType;

  @Column(name = "period_start", nullable = false)
  private LocalDateTime periodStart;

  @Column(name = "period_end", nullable = false)
  private LocalDateTime periodEnd;

  @Column(nullable = false)
  private double score;

  @Column(nullable = false)
  private long rank;

  @Column(name = "like_count", nullable = false)
  private long likeCount;

  @Column(name = "comment_count", nullable = false)
  private long commentCount;

  @Column(name = "aggregated_at", nullable = false)
  private LocalDateTime aggregatedAt;

  @Column(name = "snapshot_id", nullable = false)
  private UUID snapshotId;

  @Builder
  public PopularReview(
      UUID reviewId,
      PeriodType periodType,
      LocalDateTime periodStart,
      LocalDateTime periodEnd,
      double score,
      long rank,
      long likeCount,
      long commentCount,
      LocalDateTime aggregatedAt,
      UUID snapshotId
  ){
    this.reviewId = reviewId;
    this.periodType = periodType;
    this.periodStart = periodStart;
    this.periodEnd = periodEnd;
    this.rank = rank;
    this.score = score;
    this.likeCount = likeCount;
    this.commentCount = commentCount;
    this.aggregatedAt = aggregatedAt;
    this.snapshotId = snapshotId;
  }

  public void updateRank(long rank) {this.rank = rank;}
}
