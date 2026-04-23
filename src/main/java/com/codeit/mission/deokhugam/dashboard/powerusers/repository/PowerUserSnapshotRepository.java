package com.codeit.mission.deokhugam.dashboard.powerusers.repository;

import com.codeit.mission.deokhugam.dashboard.PeriodType;
import com.codeit.mission.deokhugam.dashboard.StagingType;
import com.codeit.mission.deokhugam.dashboard.powerusers.entity.PowerUserSnapshot;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PowerUserSnapshotRepository extends JpaRepository<PowerUserSnapshot, UUID> {
  Optional<PowerUserSnapshot> findBySnapshotId(UUID snapshotId);

  // 주어진 periodType, stagingType 조건에 맞는 스냅샷들 중에서 createdAt이 가장 최신인 1개를 가져온다
  // 즉, 해당 조건에 맞는 가장 최신에 생성된 Snapshot을 가져옴.
  Optional<PowerUserSnapshot> findTopByPeriodTypeAndStagingTypeOrderByCreatedAtDesc(
      PeriodType periodType,
      StagingType stagingType
  );

  Boolean existsPowerUserSnapshotByPeriodTypeAndSnapshotId(
      PeriodType periodType,
      UUID snapshotId
  );

  boolean existsPowerUserSnapshotByPeriodType(PeriodType periodType);
}
