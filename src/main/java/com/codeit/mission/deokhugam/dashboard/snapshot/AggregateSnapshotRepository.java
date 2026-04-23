package com.codeit.mission.deokhugam.dashboard.snapshot;

import com.codeit.mission.deokhugam.dashboard.DomainType;
import com.codeit.mission.deokhugam.dashboard.PeriodType;
import com.codeit.mission.deokhugam.dashboard.StagingType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AggregateSnapshotRepository extends JpaRepository<AggregateSnapshot, UUID> {
  Optional<AggregateSnapshot> findBySnapshotId(UUID snapshotId);

  Optional<AggregateSnapshot> findTopByDomainTypeAndPeriodTypeAndStagingTypeOrderByCreatedAtDesc(
      DomainType domainType,
      PeriodType periodType,
      StagingType stagingType
  );
}
