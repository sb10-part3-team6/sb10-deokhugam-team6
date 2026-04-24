package com.codeit.mission.deokhugam.dashboard.powerusers.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.codeit.mission.deokhugam.config.QuerydslConfig;
import com.codeit.mission.deokhugam.dashboard.PeriodType;
import com.codeit.mission.deokhugam.dashboard.StagingType;
import com.codeit.mission.deokhugam.dashboard.powerusers.entity.PowerUserSnapshot;
import com.codeit.mission.deokhugam.dashboard.powerusers.repository.PowerUserSnapshotRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(QuerydslConfig.class)
class PowerUserSnapshotRepositoryTest {

  @Autowired
  private PowerUserSnapshotRepository powerUserSnapshotRepository;

  @Autowired
  private EntityManager em;

  @Test
  @DisplayName("스냅샷 Id 조회 시 해당 스냅샷 조회 성공")
  void findBySnapshotId_returnsSnapshot() {
    // given
    UUID snapshotId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    PowerUserSnapshot snapshot =
        persistSnapshot(snapshotId, PeriodType.DAILY, StagingType.STAGING, LocalDateTime.of(2026, 4, 20, 0, 0));

    em.flush();
    em.clear();

    // when
    Optional<PowerUserSnapshot> result = powerUserSnapshotRepository.findBySnapshotId(snapshotId);

    // then
    assertTrue(result.isPresent());
    assertEquals(snapshotId, result.get().getSnapshotId());
    assertEquals(snapshot.getPeriodType(), result.get().getPeriodType());
    assertEquals(snapshot.getStagingType(), result.get().getStagingType());
  }

  @Test
  @DisplayName("findTopByPeriodTypeAndStagingTypeOrderByCreatedAtDesc가 조건에 맞는 최신 Snapshot을 가져옴 (성공)")
  void findTopByPeriodTypeAndStagingTypeOrderByCreatedAtDesc_returnsLatestPublished() {
    // given
    PowerUserSnapshot olderPublished =
        persistSnapshot(
            UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),
            PeriodType.WEEKLY,
            StagingType.PUBLISHED,
            LocalDateTime.of(2026, 4, 20, 0, 0));
    PowerUserSnapshot latestPublished =
        persistSnapshot(
            UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"),
            PeriodType.WEEKLY,
            StagingType.PUBLISHED,
            LocalDateTime.of(2026, 4, 20, 1, 0));
    persistSnapshot(
        UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd"),
        PeriodType.WEEKLY,
        StagingType.STAGING,
        LocalDateTime.of(2026, 4, 20, 2, 0));
    persistSnapshot(
        UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee"),
        PeriodType.MONTHLY,
        StagingType.PUBLISHED,
        LocalDateTime.of(2026, 4, 20, 3, 0));

    em.flush();
    updateCreatedAt(olderPublished.getId(), LocalDateTime.of(2026, 4, 20, 10, 0));
    updateCreatedAt(latestPublished.getId(), LocalDateTime.of(2026, 4, 20, 11, 0));
    em.clear();

    // when
    Optional<PowerUserSnapshot> result =
        powerUserSnapshotRepository.findTopByPeriodTypeAndStagingTypeOrderByCreatedAtDesc(
            PeriodType.WEEKLY, StagingType.PUBLISHED);

    // then
    assertTrue(result.isPresent());
    assertEquals(
        UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"),
        result.get().getSnapshotId());
  }

  @Test
  @DisplayName("existsPowerUserSnapshotByPeriodTypeAndSnapshotId가 조건에 맞는 Snapshot이 존재하는지 확인 (성공)")
  void existsPowerUserSnapshotByPeriodTypeAndSnapshotId_matchesExactPair() {
    // given
    UUID snapshotId = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");
    persistSnapshot(snapshotId, PeriodType.MONTHLY, StagingType.STAGING, LocalDateTime.of(2026, 4, 20, 0, 0));

    em.flush();
    em.clear();

    // when + then
    assertTrue(
        powerUserSnapshotRepository.existsPowerUserSnapshotByPeriodTypeAndSnapshotId(
            PeriodType.MONTHLY, snapshotId));
    assertFalse(
        powerUserSnapshotRepository.existsPowerUserSnapshotByPeriodTypeAndSnapshotId(
            PeriodType.DAILY, snapshotId));
    assertFalse(
        powerUserSnapshotRepository.existsPowerUserSnapshotByPeriodTypeAndSnapshotId(
            PeriodType.MONTHLY, UUID.fromString("11111111-2222-3333-4444-555555555555")));
  }


  @Test
  @DisplayName("existsPowerUserSnapshotByPeriodType가 조건에 맞는 Snapshot을 가져옴 (성공)")
  void existsPowerUserSnapshotByPeriodType_returnsPresence() {
    // given
    persistSnapshot(
        UUID.fromString("99999999-8888-7777-6666-555555555555"),
        PeriodType.DAILY,
        StagingType.STAGING,
        LocalDateTime.of(2026, 4, 20, 0, 0));

    em.flush();
    em.clear();

    // when + then
    assertTrue(powerUserSnapshotRepository.existsPowerUserSnapshotByPeriodType(PeriodType.DAILY));
    assertFalse(powerUserSnapshotRepository.existsPowerUserSnapshotByPeriodType(PeriodType.WEEKLY));
  }

  private PowerUserSnapshot persistSnapshot(
      UUID snapshotId,
      PeriodType periodType,
      StagingType stagingType,
      LocalDateTime aggregatedAt) {
    PowerUserSnapshot snapshot =
        PowerUserSnapshot.builder()
            .snapshotId(snapshotId)
            .periodType(periodType)
            .aggregatedAt(aggregatedAt)
            .stagingType(stagingType)
            .build();
    em.persist(snapshot);
    return snapshot;
  }

  private void updateCreatedAt(UUID id, LocalDateTime createdAt) {
    em.createQuery("update PowerUserSnapshot p set p.createdAt = :createdAt where p.id = :id")
        .setParameter("createdAt", createdAt)
        .setParameter("id", id)
        .executeUpdate();
  }
}
