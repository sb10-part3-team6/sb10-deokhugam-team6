package com.codeit.mission.deokhugam.dashboard.snapshot;

import com.codeit.mission.deokhugam.dashboard.DomainType;
import com.codeit.mission.deokhugam.dashboard.PeriodType;
import com.codeit.mission.deokhugam.dashboard.StagingType;
import com.codeit.mission.deokhugam.dashboard.exceptions.DomainTypeNotEqualException;
import com.codeit.mission.deokhugam.dashboard.exceptions.SnapshotNotFoundException;
import com.codeit.mission.deokhugam.dashboard.exceptions.SnapshotNotStagedPublishException;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AggregateSnapshotService {

  private final AggregateSnapshotRepository snapshotRepository;

  private final CacheManager cacheManager; // 캐시 매니저 주입

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
    evictDashboardCache(domainType);
  }

  // 스냅샷을 새로 Publish 하고나서, Redis 캐시에 남아있는 이전의 값들을 정리하는 메서드
  private void evictDashboardCache(DomainType domainType) {
    // 도메인 타입에 맞는 캐시를 (캐시 이름을) 대입한다.
    String cacheName = switch (domainType) {
      case POPULAR_BOOK -> "popularBooks";
      case POPULAR_REVIEW -> "popularReviews";
      case POWER_USER -> "powerUsers";
    };

    // cacheName에 해당되는 캐시를 구하고,
    Cache cache = cacheManager.getCache(cacheName);

    // cache 안에 이전 값들이 남아있으면 clear한다.
    if (cache != null) {
      cache.clear();
    }
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
