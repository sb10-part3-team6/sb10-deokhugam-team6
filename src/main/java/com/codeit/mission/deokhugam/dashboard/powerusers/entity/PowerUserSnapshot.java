package com.codeit.mission.deokhugam.dashboard.powerusers.entity;

import com.codeit.mission.deokhugam.base.BaseEntity;
import com.codeit.mission.deokhugam.dashboard.PeriodType;
import com.codeit.mission.deokhugam.dashboard.StagingType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Entity
@Table(name = "power_user_snapshot",
indexes = {
    // periodType별, stagingType 별로 찾기 때문에 해당 컬럼에 인덱스를 설정하였음.
    @Index(name = "idx_power_user_snapshots_period_status", columnList = "period_type, staging_type")
})
public class PowerUserSnapshot extends BaseEntity {

  @Column(name = "snapshot_id", nullable = false, unique = true)
  private UUID snapshotId;

  @Enumerated(EnumType.STRING)
  @Column(name = "period_type", nullable = false)
  PeriodType periodType;

  @Column(name = "aggregated_at", nullable = false)
  LocalDateTime aggregatedAt;

  @Enumerated(EnumType.STRING)
  @Column(name = "staging_type", nullable = false)
  StagingType stagingType;

  @Builder
  public PowerUserSnapshot(
      UUID snapshotId,
      PeriodType periodType,
      LocalDateTime aggregatedAt,
      StagingType stagingType
  ){
    this.snapshotId = snapshotId;
    this.periodType = periodType;
    this.aggregatedAt = aggregatedAt;
    this.stagingType = stagingType;
  }

  public PowerUserSnapshot() {
  }

  public void publish(){
    this.stagingType = StagingType.PUBLISHED;
  }

  public void archive() {this.stagingType = StagingType.ARCHIVED; }

  public void fail(){
    this.stagingType = StagingType.FAILED;
  }
}
