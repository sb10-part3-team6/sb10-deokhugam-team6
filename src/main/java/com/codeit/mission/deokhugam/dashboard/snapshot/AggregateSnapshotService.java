package com.codeit.mission.deokhugam.dashboard.snapshot;

import com.codeit.mission.deokhugam.dashboard.DomainType;
import com.codeit.mission.deokhugam.dashboard.PeriodType;
import com.codeit.mission.deokhugam.dashboard.StagingType;
import com.codeit.mission.deokhugam.dashboard.exceptions.DomainTypeNotEqualException;
import com.codeit.mission.deokhugam.dashboard.exceptions.SnapshotIdNotEqualException;
import com.codeit.mission.deokhugam.dashboard.exceptions.SnapshotNotFoundException;
import com.codeit.mission.deokhugam.dashboard.exceptions.SnapshotNotStagedPublishException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AggregateSnapshotService {
  private final AggregateSnapshotRepository snapshotRepository;

  // 새로운 스냅샷을 생성하는 서비스 -> Batch Job의 CreateNewSnapshot
  @Transactional
  public AggregateSnapshot createStagingSnapshot(
      DomainType domainType,
      PeriodType periodType,
      LocalDateTime aggregatedAt
  ) {
    snapshotRepository
        .findTopByDomainTypeAndPeriodTypeAndStagingTypeOrderByCreatedAtDesc(
            domainType, periodType, StagingType.STAGING)
        .ifPresent(AggregateSnapshot::archive);

    AggregateSnapshot snapshot = AggregateSnapshot.builder()
        .snapshotId(UUID.randomUUID())
        .periodType(periodType)
        .domainType(domainType)
        .aggregatedAt(aggregatedAt)
        .stagingType(StagingType.STAGING)
        .build();

    return snapshotRepository.save(snapshot);
  }

//  @Transactional
//  public void archiveSnapshot(UUID snapshotId) {
//    Optional<AggregateSnapshot> snapshot = snapshotRepository.findBySnapshotId(snapshotId);
//
//    if (snapshot.isEmpty()) {
//      //log.warn("Snapshot not found for archiving: {}", snapshotId);
//      return;
//    }
//    snapshot
//        .filter(s -> s.getStagingType() == StagingType.STAGING)
//        .ifPresent(AggregateSnapshot::archive);
//  }

  @Transactional
  public void publishSnapshot(DomainType domainType, UUID snapshotId) {
    // 조회하고자 하는 스냅샷의 존재 여부를 확인하고 가져옴.
    AggregateSnapshot newSnapshot = snapshotRepository.findBySnapshotId(snapshotId)
        .orElseThrow(SnapshotNotFoundException::new);

    // 스냅샷의 도메인 타입이 서로 일치하는지 확인
    if (!Objects.equals(newSnapshot.getDomainType(), domainType)) {
      throw new DomainTypeNotEqualException(Map.of("newSnapshotId", newSnapshot.getSnapshotId(),
          "foundSnapshotId", domainType));
    }

    // 스냅샷의 상태를 확인
    if (newSnapshot.getStagingType() != StagingType.STAGING) {
      throw new SnapshotNotStagedPublishException(Map.of("snapshotId", newSnapshot.getId(),
          "stagingType", newSnapshot.getStagingType()));
    }

    snapshotRepository
        .findTopByDomainTypeAndPeriodTypeAndStagingTypeOrderByCreatedAtDesc(
            domainType, newSnapshot.getPeriodType(), StagingType.PUBLISHED)
        .filter(oldSnapshot -> !oldSnapshot.getSnapshotId().equals(snapshotId))
        .ifPresent(AggregateSnapshot::archive);

    newSnapshot.publish();
  }

  @Transactional
  public void failSnapshot(UUID snapshotId) {
    snapshotRepository.findBySnapshotId(snapshotId)
        .filter(snapshot -> snapshot.getStagingType() == StagingType.STAGING)
        .ifPresent(AggregateSnapshot::fail);
  }

  @Transactional(readOnly = true)
  public UUID getPublishedSnapshotId(
      DomainType domainType,
      PeriodType periodType
  ) {
    return snapshotRepository
        .findTopByDomainTypeAndPeriodTypeAndStagingTypeOrderByCreatedAtDesc(
            domainType, periodType, StagingType.PUBLISHED)
        .map(AggregateSnapshot::getSnapshotId)
        .orElseThrow(SnapshotNotFoundException::new);
  }
}
