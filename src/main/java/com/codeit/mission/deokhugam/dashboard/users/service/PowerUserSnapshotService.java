package com.codeit.mission.deokhugam.dashboard.users.service;

import com.codeit.mission.deokhugam.dashboard.PeriodType;
import com.codeit.mission.deokhugam.dashboard.StagingType;
import com.codeit.mission.deokhugam.dashboard.users.entity.PowerUserSnapshot;
import com.codeit.mission.deokhugam.dashboard.users.exception.SnapshotNotFoundException;
import com.codeit.mission.deokhugam.dashboard.users.repository.PowerUserSnapshotRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PowerUserSnapshotService {
  private final PowerUserSnapshotRepository powerUserSnapshotRepository;

  @Transactional
  public PowerUserSnapshot createStagingSnapshot(
      PeriodType periodType, LocalDateTime aggregatedAt) {
    PowerUserSnapshot snapshot = PowerUserSnapshot.builder()
        .snapshotId(UUID.randomUUID())
        .periodType(periodType)
        .aggregatedAt(aggregatedAt)
        .stagingType(StagingType.STAGING)
        .build();

    return powerUserSnapshotRepository.save(snapshot);
  }

  @Transactional
  public void failSnapshot(UUID snapshotId) {
    powerUserSnapshotRepository.findBySnapshotId(snapshotId)
        .filter(snapshot -> snapshot.getStagingType() == StagingType.STAGING)
        .ifPresent(PowerUserSnapshot::fail);
  }

  // 스냅샷을 Staging -> publish로 바꿔 조회되게끔 하는 메서드
  @Transactional
  public void publishSnapshot(UUID snapshotId) {
    PowerUserSnapshot newSnapshot = powerUserSnapshotRepository.findBySnapshotId(snapshotId)
        .orElseThrow(SnapshotNotFoundException::new);

    PeriodType periodType = newSnapshot.getPeriodType();

    // 가장 최신에 생성된 PeriodType과 StagingType이 published 인 스냅샷을 조회하고
    // 해당 스냅샷이 스텝 1에서 생성된 새로운 스냅샷과 동일한지 확인한 후
    // 동일하지 않다면 해당 oldSnapshot을 archive 상태로 돌려놓음
    powerUserSnapshotRepository
        .findTopByPeriodTypeAndStagingTypeOrderByCreatedAtDesc(periodType, StagingType.PUBLISHED)
        .filter(oldSnapshot -> !oldSnapshot.getSnapshotId().equals(snapshotId))
        .ifPresent(PowerUserSnapshot::archive);

    // 그 후 새로 생긴 스냅샷은 Publish함.
    newSnapshot.publish();
  }
}
