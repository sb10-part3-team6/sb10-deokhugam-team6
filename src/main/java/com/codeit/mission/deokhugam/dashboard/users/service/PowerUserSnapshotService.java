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

  // 새로운 파워유저의 스냅샷 객체를 생성한다. (스냅샷 -> 메타 데이터)
  @Transactional
  public void createStagingSnapshot(
      PeriodType periodType, LocalDateTime aggregatedAt, UUID snapshotId) {
    PowerUserSnapshot snapshot = PowerUserSnapshot.builder()
        .snapshotId(snapshotId)
        .periodType(periodType)
        .aggregatedAt(aggregatedAt)
        .stagingType(StagingType.STAGING) // 처음 생성된 스냅샷 객체의 스테이징 타입은 Staging
        // 추후 publishSnapshot 메소드에 의해 Publish 될 수 있음.
        .build();

    powerUserSnapshotRepository.save(snapshot);
  }

  // 파워유저 스냅샷 객체의 스테이징 타입을 Staging -> Publish로 바꿈
  // 배치 작업에 의해 실행되는 메서드
  // 새롭게 생성된 스냅샷 (Staging)을 적용하기 위해 (Publish)로  바꿈
  @Transactional
  public void publishSnapshot(UUID snapshotId) {
    // 배치 잡 파라미터로 받은 snapshotId를 통해 스냅샷 객체 가져옴
    PowerUserSnapshot newSnapshot = powerUserSnapshotRepository.findBySnapshotId(snapshotId)
        .orElseThrow(SnapshotNotFoundException::new);

    // 스냅샷의 기간 타입(일간,주간,월간,상시) 종류를 가져온다.
    PeriodType periodType = newSnapshot.getPeriodType();

    // 기간에 해당하는 publish된 snapshot을 archive로 바꾼다.
    powerUserSnapshotRepository
        .findTopByPeriodTypeAndStagingTypeOrderByCreatedAtDesc(periodType, StagingType.PUBLISHED)
        .filter(oldSnapshot -> !oldSnapshot.getSnapshotId().equals(snapshotId))
        .ifPresent(PowerUserSnapshot::archive);

    // 그 다음 새로 생성한 스냅샷 객체를 publish
    newSnapshot.publish();
  }
}
