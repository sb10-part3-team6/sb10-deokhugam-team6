package com.codeit.mission.deokhugam.dashboard.snapshot;

import com.codeit.mission.deokhugam.base.BaseEntity;
import com.codeit.mission.deokhugam.dashboard.DomainType;
import com.codeit.mission.deokhugam.dashboard.PeriodType;
import com.codeit.mission.deokhugam.dashboard.StagingType;
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

@Getter
@Entity
@Table(name = "aggregate_snapshot",
    indexes = {
        // periodType별, stagingType 별로 찾기 때문에 해당 컬럼에 인덱스를 설정하였음.
        @Index(name = "idx_aggregate_domain_snapshots_period_status",
            columnList = "domain_type, period_type, staging_type")
    })
public class AggregateSnapshot extends BaseEntity {

  @Column(name = "snapshot_id", nullable = false, unique = true)
  private UUID snapshotId;

  @Enumerated(EnumType.STRING)
  @Column(name = "period_type", nullable = false)
  PeriodType periodType;

  @Enumerated(EnumType.STRING)
  @Column(name = "domain_type", nullable = false)
  DomainType domainType;

  @Column(name = "aggregated_at", nullable = false)
  LocalDateTime aggregatedAt;

  @Enumerated(EnumType.STRING)
  @Column(name = "staging_type", nullable = false)
  StagingType stagingType;

  @Builder
  public AggregateSnapshot(
      UUID snapshotId,
      PeriodType periodType,
      DomainType domainType,
      LocalDateTime aggregatedAt,
      StagingType stagingType
  ){
    this.snapshotId = snapshotId;
    this.periodType = periodType;
    this.domainType = domainType;
    this.aggregatedAt = aggregatedAt;
    this.stagingType = stagingType;
  }

  public AggregateSnapshot() {
  }

  public void publish(){
    this.stagingType = StagingType.PUBLISHED;
  }

  public void archive() {this.stagingType = StagingType.ARCHIVED; }

  public void fail(){
    this.stagingType = StagingType.FAILED;
  }
}
