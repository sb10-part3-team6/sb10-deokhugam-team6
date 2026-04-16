package com.codeit.mission.deokhugam.dashboard.users.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.codeit.mission.deokhugam.dashboard.PeriodType;
import com.codeit.mission.deokhugam.dashboard.users.dto.PowerUserDto;
import com.codeit.mission.deokhugam.dashboard.users.entity.PowerUser;
import com.codeit.mission.deokhugam.user.entity.User;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

@DataJpaTest
class PowerUserRepositoryTest {

  @Autowired
  private PowerUserRepository powerUserRepository;

  @Autowired
  private EntityManager em;

  @Test
  @DisplayName("기간 별 PowerUser의 개수 검증")
  void countLatestRankingsByPeriodType_success(){
    // given
    LocalDateTime periodStart = LocalDateTime.of(2026,4,17,0,0);
    LocalDateTime periodEnd = LocalDateTime.of(2026,4,24,0,0);


    PowerUser p1= PowerUser.builder()
        .userId(UUID.randomUUID())
        .periodType(PeriodType.WEEKLY)
        .periodStart(periodStart)
        .periodEnd(periodEnd)
        .rank(1L)
        .score(30.0)
        .reviewScoreSum(20.0)
        .likeCount(3L)
        .commentCount(1L)
        .aggregatedAt(periodEnd)
        .build();

    PowerUser p2 = PowerUser.builder()
        .userId(UUID.randomUUID())
        .periodType(PeriodType.WEEKLY)
        .periodStart(periodStart)
        .periodEnd(periodEnd)
        .rank(1L)
        .score(30.0)
        .reviewScoreSum(20.0)
        .likeCount(3L)
        .commentCount(1L)
        .aggregatedAt(periodEnd)
        .build();

    PowerUser p3 = PowerUser.builder()
        .userId(UUID.randomUUID())
        .periodType(PeriodType.WEEKLY)
        .periodStart(periodStart)
        .periodEnd(periodEnd)
        .rank(1L)
        .score(30.0)
        .reviewScoreSum(20.0)
        .likeCount(3L)
        .commentCount(1L)
        .aggregatedAt(periodEnd)
        .build();
    em.persist(p1);
    em.persist(p2);
    em.persist(p3);


    // when
    long count = powerUserRepository.countLatestRankingsByPeriodType(PeriodType.WEEKLY);

    // then
    assertEquals(3, count);
  }

  @Test
  @DisplayName("동일 rank 유저 존재 시, createdAt(After) 오름차순으로 정렬한다.")
  void findLatestRankingDtosByPeriodByAsc_tieBreakByCreatedAt(){
    // Given
    User user1 = User.builder()
        .email("a@naver.com")
        .nickname("a")
        .password("a1")
        .build();

    User user2 = User.builder()
        .email("b@naver.com")
        .nickname("b")
        .password("b2")
        .build();

    em.persist(user1);
    em.persist(user2);

    LocalDateTime periodStart = LocalDateTime.of(2026,4,14,0,0);
    LocalDateTime periodEnd = LocalDateTime.of(2026,4,21,0,0);

    // 랭킹이 1인 일찍 생성된 유저
    PowerUser early = PowerUser.builder()
        .userId(user1.getId())
        .periodType(PeriodType.WEEKLY)
        .periodStart(periodStart)
        .periodEnd(periodEnd)
        .rank(1L)
        .score(30.0)
        .reviewScoreSum(20.0)
        .likeCount(3L)
        .commentCount(1L)
        .aggregatedAt(periodEnd)
        .build();

    PowerUser later = PowerUser.builder()
        .userId(user2.getId())
        .periodType(PeriodType.WEEKLY)
        .periodStart(periodStart)
        .periodEnd(periodEnd)
        .rank(1L)
        .score(30.0)
        .reviewScoreSum(20.0)
        .likeCount(3L)
        .commentCount(1L)
        .aggregatedAt(periodEnd)
        .build();

    // Reflection을 써서 필드를 강제로 세팅할 수 있었다.
    ReflectionTestUtils.setField(early, "createdAt", LocalDateTime.of(2026, 4, 21, 0, 0));
    ReflectionTestUtils.setField(later, "createdAt", LocalDateTime.of(2026, 4, 21, 0, 1));


    em.persist(early);
    em.persist(later);
    em.flush(); // 영속성 컨텍스트 내보내기
    em.clear(); // 영속성 컨텍스트 비움.

    // When
    List<PowerUserDto> result =
        powerUserRepository.findLatestRankingDtosByPeriodTypeAsc(PeriodType.WEEKLY, null, null,
            PageRequest.of(0, 10));

    // Then
    assertEquals(2, result.size());
    assertEquals("a", result.get(0).nickname());
    assertEquals("b", result.get(1).nickname());


  }

  @Test
  @DisplayName("동일 rank이면 DESC 조회에서 createdAt 내림차순으로 정렬된다")
  void findLatestRankingDtosByPeriodTypeDesc_tieBreakByCreatedAt() {
    User user1 = User.builder()
        .email("a@test.com")
        .nickname("a")
        .password("password")
        .build();

    User user2 = User.builder()
        .email("b@test.com")
        .nickname("b")
        .password("password")
        .build();

    // 유저 객체를 영속화.
    em.persist(user1);
    em.persist(user2);

    // 4월 14일부터 4월 21일까지를 기준으로 함
    LocalDateTime periodStart = LocalDateTime.of(2026, 4, 14, 0, 0);
    LocalDateTime periodEnd = LocalDateTime.of(2026, 4, 21, 0, 0);

    // 랭크(커서)가 2인 일찍 생성된 파워 유저
    PowerUser early = PowerUser.builder()
        .userId(user1.getId())
        .periodType(PeriodType.WEEKLY)
        .periodStart(periodStart)
        .periodEnd(periodEnd)
        .rank(2L)
        .score(30.0)
        .reviewScoreSum(20.0)
        .likeCount(3L)
        .commentCount(1L)
        .aggregatedAt(LocalDateTime.of(2026, 4, 21, 0, 0))
        .build();

    // 랭크(커서)가 2인 늦게 생성된 파워 유저
    PowerUser late = PowerUser.builder()
        .userId(user2.getId())
        .periodType(PeriodType.WEEKLY)
        .periodStart(periodStart)
        .periodEnd(periodEnd)
        .rank(2L)
        .score(29.0)
        .reviewScoreSum(19.0)
        .likeCount(2L)
        .commentCount(1L)
        .aggregatedAt(LocalDateTime.of(2026, 4, 21, 0, 0))
        .build();

    // 리플렉션을 사용하여 강제로 필드를 세팅
    ReflectionTestUtils.setField(early, "createdAt", LocalDateTime.of(2026, 4, 21, 0, 0));
    ReflectionTestUtils.setField(late, "createdAt", LocalDateTime.of(2026, 4, 21, 0, 1));

    // 파워 유저 객체를 영속화하고 영속성 컨텍스트를 비움.
    em.persist(early);
    em.persist(late);
    em.flush();
    em.clear();

    // When
    List<PowerUserDto> result =
        powerUserRepository.findLatestRankingDtosByPeriodTypeDesc(
            PeriodType.WEEKLY,
            null,
            null,
            PageRequest.of(0, 10)
        );

    // Then
    assertEquals(2, result.size());
    assertEquals("b", result.get(0).nickname());
    assertEquals("a", result.get(1).nickname());
  }


}
