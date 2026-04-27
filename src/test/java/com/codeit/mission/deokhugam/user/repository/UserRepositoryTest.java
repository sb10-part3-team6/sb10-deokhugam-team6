package com.codeit.mission.deokhugam.user.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeit.mission.deokhugam.config.JpaAuditingConfig;
import com.codeit.mission.deokhugam.config.QuerydslConfig;
import com.codeit.mission.deokhugam.user.entity.User;
import com.codeit.mission.deokhugam.user.entity.UserStatus;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@Import({JpaAuditingConfig.class, QuerydslConfig.class})
class UserRepositoryTest {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private EntityManager entityManager;

  @Test
  @DisplayName("논리 삭제 후 1일이 지난 유저만 정확히 조회되는지 확인")
  void findDeletedUserIdsOlderThan_Success() {
    // given
    Instant now = Instant.now();
    User oldDeletedUser = createUser("old@example.com", "오래된탈퇴자");
    User recentDeletedUser = createUser("recent@example.com", "최근탈퇴자");
    User activeUser = createUser("active@example.com", "활동중유저");

    userRepository.saveAll(List.of(oldDeletedUser, recentDeletedUser, activeUser));
    entityManager.flush();

    // 직접 쿼리로 상태와 시간을 조작
    updateUserStatusAndTime(oldDeletedUser.getId(), UserStatus.DELETED,
        now.minus(2, ChronoUnit.DAYS));
    updateUserStatusAndTime(recentDeletedUser.getId(), UserStatus.DELETED,
        now.minus(1, ChronoUnit.HOURS));

    entityManager.clear();

    // when
    Instant threshold = now.minus(1, ChronoUnit.DAYS);
    Page<UUID> deletedIds = userRepository.findDeletedUserIdsOlderThan(threshold,
        PageRequest.of(0, 10));

    // then
    assertThat(deletedIds.getContent()).hasSize(1);
    // H2에서 Native Query를 실행하면 UUID가 아니라 byte로 나오게 돼서 다시 UUID로 변환하는 로직
    Object result = deletedIds.getContent().get(0);
    if (result instanceof byte[]) {
      java.nio.ByteBuffer bb = java.nio.ByteBuffer.wrap((byte[]) result);
      UUID uuid = new UUID(bb.getLong(), bb.getLong());
      assertThat(uuid).isEqualTo(oldDeletedUser.getId());
    } else {
      assertThat(result).isEqualTo(oldDeletedUser.getId());
    }
  }

  @Test
  @DisplayName("물리 삭제(Hard Delete) 쿼리가 대상 유저만 정확히 삭제하는지 확인")
  void hardDeleteByIds_Success() {
    // given
    Instant now = Instant.now();
    User targetUser = createUser("target@example.com", "오래된탈퇴자");
    User activeUser = createUser("active@example.com", "활동중유저");
    User recentDeletedUser = createUser("recent@example.com", "최근탈퇴자");

    userRepository.saveAll(List.of(targetUser, activeUser, recentDeletedUser));
    entityManager.flush();

    updateUserStatusAndTime(targetUser.getId(), UserStatus.DELETED,
        now.minus(2, ChronoUnit.DAYS));
    updateUserStatusAndTime(recentDeletedUser.getId(), UserStatus.DELETED,
        now);
    entityManager.clear();

    // when
    Instant threshold = now.minus(1, ChronoUnit.DAYS);
    userRepository.hardDeleteByIds(
        List.of(targetUser.getId(), activeUser.getId(), recentDeletedUser.getId()), threshold);

    // then
    assertThat(countById(targetUser.getId())).isZero();
    assertThat(countById(activeUser.getId())).isEqualTo(1);
    assertThat(countById(recentDeletedUser.getId())).isEqualTo(1);
  }

  @Test
  @DisplayName("existsByDeletedUser: 상태가 DELETED인 유저만 존재 여부를 확인하는지 검증")
  void existsByDeletedUser_Success() {
    // given
    User deletedUser = createUser("deleted@example.com", "탈퇴자");
    User activeUser = createUser("active@example.com", "활동중");

    userRepository.saveAll(List.of(deletedUser, activeUser));
    entityManager.flush();

    updateUserStatusAndTime(deletedUser.getId(), UserStatus.DELETED, Instant.now());
    entityManager.clear();

    // when & then
    assertThat(userRepository.existsByDeletedUser(deletedUser.getId())).isTrue();
    assertThat(userRepository.existsByDeletedUser(activeUser.getId())).isFalse();
    assertThat(userRepository.existsByDeletedUser(UUID.randomUUID())).isFalse();
  }

  @Test
  @DisplayName("hardDeleteById: 상태가 DELETED인 유저만 물리 삭제하고 삭제 건수를 반환하는지 검증")
  void hardDeleteById_Success() {
    // given
    User deletedUser = createUser("deleted@example.com", "탈퇴자");
    User activeUser = createUser("active@example.com", "활동중");

    userRepository.saveAll(List.of(deletedUser, activeUser));
    entityManager.flush();

    updateUserStatusAndTime(deletedUser.getId(), UserStatus.DELETED, Instant.now());
    entityManager.clear();

    // when
    int deletedCount = userRepository.hardDeleteById(deletedUser.getId());
    int activeDeletedCount = userRepository.hardDeleteById(activeUser.getId());
    int nonExistentDeletedCount = userRepository.hardDeleteById(UUID.randomUUID());

    // then
    assertThat(deletedCount).isEqualTo(1);
    assertThat(activeDeletedCount).isEqualTo(0);
    assertThat(nonExistentDeletedCount).isEqualTo(0);

    assertThat(countById(deletedUser.getId())).isZero();
    assertThat(countById(activeUser.getId())).isEqualTo(1);
  }

  private int countById(UUID id) {
    Number count = (Number) entityManager.createNativeQuery(
            "SELECT count(*) FROM users WHERE id = ?1")
        .setParameter(1, id)
        .getSingleResult();
    return count.intValue();
  }

  private User createUser(String email, String nickname) {
    return User.builder()
        .email(email)
        .nickname(nickname)
        .password("Password123!")
        .build();
  }

  @Transactional
  protected void updateUserStatusAndTime(UUID id, UserStatus status, Instant time) {
    entityManager.createNativeQuery("UPDATE users SET status = ?1, updated_at = ?2 WHERE id = ?3")
        .setParameter(1, status.name())
        .setParameter(2, java.sql.Timestamp.from(time))
        .setParameter(3, id)
        .executeUpdate();
  }
}
