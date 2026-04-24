package com.codeit.mission.deokhugam.dashboard.popularbooks.entity;

import com.codeit.mission.deokhugam.base.BaseEntity;
import com.codeit.mission.deokhugam.dashboard.PeriodType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(
    name = "popular_books",
    indexes = {
        @Index(name="idx_book_id_period_type_snap_shot_id", columnList = "book_id, period_type, snapshot_id")
    }
)

public class PopularBook extends BaseEntity {
  @Column(name = "book_id", nullable = false)
  private UUID bookId;

  @Column(name = "period_start", nullable = false)
  private LocalDateTime periodStart;

  @Column(name = "period_end", nullable = false)
  private LocalDateTime periodEnd;

  @Column(name = "review_count", nullable = false)
  private Long reviewCount;

  @Column(name = "avgRating", nullable = false)
  private double avgRating;

  @Column(name = "score" ,nullable = false)
  private double score;

  @Column(name = "rank", nullable = false)
  private Long rank;

  @Column(name = "period_type", nullable = false)
  private PeriodType periodType;

  @Column(name = "snapshot_id", nullable = false)
  private UUID snapshotId;

  @Builder
  public PopularBook(
      UUID bookId,
      LocalDateTime periodStart,
      LocalDateTime periodEnd,
      Long reviewCount,
      double avgRating,
      double score,
      Long rank,
      PeriodType periodType,
      UUID snapshotId
  ){
    this.bookId = bookId;
    this.periodEnd = periodEnd;
    this.periodStart = periodStart;
    this.reviewCount = reviewCount;
    this.avgRating = avgRating;
    this.score = score;
    this.rank = rank;
    this.periodType = periodType;
    this.snapshotId = snapshotId;
  }
}
