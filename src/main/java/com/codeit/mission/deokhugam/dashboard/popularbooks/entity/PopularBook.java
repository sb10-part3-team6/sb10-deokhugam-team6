package com.codeit.mission.deokhugam.dashboard.popularbooks.entity;

import com.codeit.mission.deokhugam.base.BaseEntity;
import com.codeit.mission.deokhugam.dashboard.PeriodType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;

@Entity
@Getter
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

  @Column(name = "comment_count", nullable = false)
  private Long commentCount;

  @Column(name = "score" ,nullable = false)
  private double score;

  @Column(name = "rank", nullable = false)
  private Long rank;

  @Column(name = "period_type", nullable = false)
  private PeriodType periodType;

  @Column(name = "snapshot_id", nullable = false)
  private UUID snapshotId;
}
