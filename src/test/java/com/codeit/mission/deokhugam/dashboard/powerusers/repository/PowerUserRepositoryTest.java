package com.codeit.mission.deokhugam.dashboard.powerusers.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.codeit.mission.deokhugam.config.QuerydslConfig;
import com.codeit.mission.deokhugam.dashboard.PeriodType;
import com.codeit.mission.deokhugam.dashboard.powerusers.dto.PowerUserDto;
import com.codeit.mission.deokhugam.dashboard.powerusers.entity.PowerUser;
import com.codeit.mission.deokhugam.dashboard.powerusers.repository.PowerUserRepository;
import com.codeit.mission.deokhugam.user.entity.User;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;

@DataJpaTest
@Import(QuerydslConfig.class)
class PowerUserRepositoryTest {

  private static final UUID SNAPSHOT_ID =
      UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
  private static final UUID OTHER_SNAPSHOT_ID =
      UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

  @Autowired
  private PowerUserRepository powerUserRepository;

  @Autowired
  private EntityManager em;

  @Test
  @DisplayName("해당 스냅샷에 해당하는 PowerUser만 개수 세기")
  void countRankingsBySnapshotId_countsTargetSnapshotOnly() {
    // given
    LocalDateTime periodStart = LocalDateTime.of(2026, 4, 14, 0, 0);
    LocalDateTime periodEnd = LocalDateTime.of(2026, 4, 21, 0, 0);

    // 유저 3명 생성 및 영속화
    User user1 = persistUser("user1@test.com", "user1");
    User user2 = persistUser("user2@test.com", "user2");
    User user3 = persistUser("user3@test.com", "user3");

    // 3명의 유저 정보를 바탕으로 1,2는 찾고자하는 스냅샷 Id, 3은 다른 Snapshot Id를 부여하여 파워 유저로 영속화
    persistPowerUser(user1, 1L, 30.0, periodStart, periodEnd, SNAPSHOT_ID);
    persistPowerUser(user2, 2L, 20.0, periodStart, periodEnd, SNAPSHOT_ID);
    persistPowerUser(user3, 1L, 15.0, periodStart, periodEnd, OTHER_SNAPSHOT_ID);

    em.flush();
    em.clear();

    // when
    long count = powerUserRepository.countRankingsBySnapshotId(SNAPSHOT_ID);

    // then
    assertEquals(2L, count); // 2개가 나와야 함.
  }

  @Test
  @DisplayName("한 스냅샷 내에서 동점자가 발생햇을 때 오름차순으로 정렬 테스트")
  void findRankingDtosBySnapshotIdAsc_ordersByRankThenCreatedAt() {
    LocalDateTime periodStart = LocalDateTime.of(2026, 4, 14, 0, 0);
    LocalDateTime periodEnd = LocalDateTime.of(2026, 4, 21, 0, 0);

    User user1 = persistUser("a@test.com", "a");
    User user2 = persistUser("b@test.com", "b");
    User ignoredUser = persistUser("ignored@test.com", "ignored");

    PowerUser early = persistPowerUser(user1, 1L, 30.0, periodStart, periodEnd, SNAPSHOT_ID);
    PowerUser later = persistPowerUser(user2, 1L, 29.0, periodStart, periodEnd, SNAPSHOT_ID);
    persistPowerUser(ignoredUser, 0L, 99.0, periodStart, periodEnd, OTHER_SNAPSHOT_ID);

    em.flush();
    updateCreatedAt(early.getId(), LocalDateTime.of(2026, 4, 21, 0, 0));
    updateCreatedAt(later.getId(), LocalDateTime.of(2026, 4, 21, 0, 1));
    em.clear();

    List<PowerUserDto> result =
        powerUserRepository.findRankingDtosBySnapshotIdAsc(
            SNAPSHOT_ID, null, null, PageRequest.of(0, 10));

    assertEquals(2, result.size());
    assertEquals("a", result.get(0).nickname());
    assertEquals("b", result.get(1).nickname());
  }

  @Test
  @DisplayName("한 스냅샷 내에서 동점이 생길 때의 내림차 순 정렬 테스트")
  void findRankingDtosBySnapshotIdDesc_appliesCursorAndTieBreak() {
    // given
    LocalDateTime periodStart = LocalDateTime.of(2026, 4, 14, 0, 0);
    LocalDateTime periodEnd = LocalDateTime.of(2026, 4, 21, 0, 0);

    // 두 명의 유저의 랭크가 2인 상황
    User rankThreeUser = persistUser("rank3@test.com", "rank3");
    User earlyRankTwoUser = persistUser("rank2a@test.com", "rank2a");
    User lateRankTwoUser = persistUser("rank2b@test.com", "rank2b");
    User rankOneUser = persistUser("rank1@test.com", "rank1");

    // 파워 유저화
    persistPowerUser(rankThreeUser, 3L, 40.0, periodStart, periodEnd, SNAPSHOT_ID);
    PowerUser earlyRankTwo =
        persistPowerUser(earlyRankTwoUser, 2L, 30.0, periodStart, periodEnd, SNAPSHOT_ID);
    PowerUser lateRankTwo =
        persistPowerUser(lateRankTwoUser, 2L, 29.0, periodStart, periodEnd, SNAPSHOT_ID);
    persistPowerUser(rankOneUser, 1L, 20.0, periodStart, periodEnd, SNAPSHOT_ID);

    em.flush();

    // 랭크가 같아도 earlyRankTwo 파워 유저가 더 일찍 만들어짐
    updateCreatedAt(earlyRankTwo.getId(), LocalDateTime.of(2026, 4, 21, 0, 0));
    updateCreatedAt(lateRankTwo.getId(), LocalDateTime.of(2026, 4, 21, 0, 1));
    em.clear();

    // when
    List<PowerUserDto> result =
        powerUserRepository.findRankingDtosBySnapshotIdDesc(
            SNAPSHOT_ID,
            2L,
            LocalDateTime.of(2026, 4, 21, 0, 1),
            PageRequest.of(0, 10));

    // then
    assertEquals(2, result.size());
    assertEquals("rank2a", result.get(0).nickname());
    assertEquals("rank1", result.get(1).nickname());
  }

  // 유저를 생성 및 영속화
  private User persistUser(String email, String nickname) {
    User user = User.builder().email(email).nickname(nickname).password("password").build();
    em.persist(user);
    return user;
  }

  // 유저 정보 바탕으로 파워 유저를 생성하고 영속화
  private PowerUser persistPowerUser(
      User user,
      long rank,
      double score,
      LocalDateTime periodStart,
      LocalDateTime periodEnd,
      UUID snapshotId) {
    PowerUser powerUser =
        PowerUser.builder()
            .userId(user.getId())
            .periodType(PeriodType.WEEKLY)
            .periodStart(periodStart)
            .periodEnd(periodEnd)
            .rank(rank)
            .score(score)
            .reviewScoreSum(score - 5.0)
            .likeCount(3L)
            .commentCount(2L)
            .aggregatedAt(periodEnd)
            .snapshotId(snapshotId)
            .build();
    em.persist(powerUser);
    return powerUser;
  }

  // createdAt을 수정하는 메서드
  private void updateCreatedAt(UUID id, LocalDateTime createdAt) {
    int updatedRows = em.createQuery("update PowerUser pu set pu.createdAt = :createdAt where pu.id = :id")
        .setParameter("createdAt", createdAt)
        .setParameter("id", id)
        .executeUpdate();

    assertEquals(1, updatedRows, "createdAt 업데이트 대상은 정확히 1건이어야 합니다.");
  }
}
