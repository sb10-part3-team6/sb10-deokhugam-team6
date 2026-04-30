package com.codeit.mission.deokhugam.dashboard.snapshot;

import com.codeit.mission.deokhugam.dashboard.DomainType;
import com.codeit.mission.deokhugam.dashboard.PeriodType;
import com.codeit.mission.deokhugam.dashboard.StagingType;
import com.codeit.mission.deokhugam.dashboard.exceptions.DomainTypeNotEqualException;
import com.codeit.mission.deokhugam.dashboard.exceptions.InvalidKeepCountException;
import com.codeit.mission.deokhugam.dashboard.exceptions.SnapshotNotFoundException;
import com.codeit.mission.deokhugam.dashboard.exceptions.SnapshotNotStagedPublishException;
import com.codeit.mission.deokhugam.dashboard.popularbooks.repository.PopularBookRepository;
import com.codeit.mission.deokhugam.dashboard.popularreviews.repository.PopularReviewRepository;
import com.codeit.mission.deokhugam.dashboard.powerusers.repository.PowerUserRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AggregateSnapshotService {

  private final AggregateSnapshotRepository snapshotRepository;
  private final PopularReviewRepository popularReviewRepository;
  private final PopularBookRepository popularBookRepository;
  private final PowerUserRepository powerUserRepository;


  // 새로운 스냅샷을 생성하는 서비스 -> Batch Job의 CreateNewSnapshot
  @Transactional
  public AggregateSnapshot createStagingSnapshot(
      DomainType domainType,
      PeriodType periodType,
      Instant aggregatedAt
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

  @Transactional
  public void publishSnapshot(DomainType domainType, UUID snapshotId) {
    // 조회하고자 하는 스냅샷의 존재 여부를 확인하고 가져옴.
    AggregateSnapshot newSnapshot = snapshotRepository.findBySnapshotId(snapshotId)
        .orElseThrow(SnapshotNotFoundException::new);

    // 스냅샷의 도메인 타입이 서로 일치하는지 확인
    if (!Objects.equals(newSnapshot.getDomainType(), domainType)) {
      throw new DomainTypeNotEqualException(Map.of(
          "newDomainType", newSnapshot.getDomainType(),
          "existingDomainType", domainType));
    }

    // 스냅샷의 상태를 확인
    if (newSnapshot.getStagingType() != StagingType.STAGING) {
      throw new SnapshotNotStagedPublishException(Map.of(
          "snapshotId", newSnapshot.getSnapshotId(),
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

  // 오래된 스냅샷들을 정리한다.
  @Transactional
  public void cleanupOldSnapshots(DomainType domainType, PeriodType periodType, int keepCount) {
    if(keepCount < 2){
      throw new InvalidKeepCountException(keepCount);
    }
    List<AggregateSnapshot> snapshots = snapshotRepository.findByDomainTypeAndPeriodTypeAndStagingTypeInOrderByCreatedAtDesc(
        domainType,
        periodType,
        List.of(StagingType.PUBLISHED, StagingType.ARCHIVED) // 퍼블리시 된 스냅샷과 아카이빙된 스냅샷을 가져옴.
    );

    List<AggregateSnapshot> targets = snapshots.stream().skip(keepCount).toList();

    if(targets.isEmpty()){
      return;
    }

    // keep Count 만큼 남겨두고 그 뒤의 스냅샷들의 id를 추출함.
    List<UUID> snapshotIds = targets.stream()
        .map(AggregateSnapshot::getSnapshotId)
        .toList();

    // 지정한 스냅샷들을 삭제합니다.
    deleteAggregateRows(domainType, snapshotIds);
    snapshotRepository.deleteAllInBatch(targets);
  }



  // 각 도메인 별 레포지토리에서 오래된 스냅샷의 엔티티들을 삭제
  private void deleteAggregateRows(DomainType domainType, List<UUID> snapshotIds) {
    switch (domainType) {
      case POPULAR_BOOK -> popularBookRepository.deleteBySnapshotIdIn(snapshotIds);
      case POPULAR_REVIEW -> popularReviewRepository.deleteBySnapshotIdIn(snapshotIds);
      case POWER_USER -> powerUserRepository.deleteBySnapshotIdIn(snapshotIds);
    }
  }

}
