package com.codeit.mission.deokhugam.dashboard.powerusers.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.codeit.mission.deokhugam.dashboard.DirectionEnum;
import com.codeit.mission.deokhugam.dashboard.DomainType;
import com.codeit.mission.deokhugam.dashboard.PeriodType;
import com.codeit.mission.deokhugam.dashboard.StagingType;
import com.codeit.mission.deokhugam.dashboard.powerusers.dto.CursorPageResponsePowerUserDto;
import com.codeit.mission.deokhugam.dashboard.powerusers.dto.PowerUserDto;
import com.codeit.mission.deokhugam.dashboard.powerusers.repository.PowerUserRepository;
import com.codeit.mission.deokhugam.dashboard.snapshot.AggregateSnapshot;
import com.codeit.mission.deokhugam.dashboard.snapshot.AggregateSnapshotRepository;
import com.codeit.mission.deokhugam.error.DeokhugamException;
import com.codeit.mission.deokhugam.error.ErrorCode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class PowerUserServiceTest {

  // 스냅샷 ID는 항상 이것으로 고정
  private static final UUID DAILY_SNAPSHOT_ID =
      UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

  @Mock
  private PowerUserRepository powerUserRepository;

  @Mock
  private AggregateSnapshotRepository aggregateSnapshotRepository;

  @InjectMocks
  private PowerUserService powerUserService;

  @Test
  @DisplayName("Publish 된 스냅샷을 가진 PowerUser를 Rank 기준 오름차순으로 조회 (첫 페이지, hasNext = false)")
  void getLatestRankings_firstPageAsc() {
    // Given
    LocalDateTime createdAt1 = LocalDateTime.of(2026, 4, 15, 10, 0);
    LocalDateTime createdAt2 = LocalDateTime.of(2026, 4, 15, 10, 1);

    // 두 개의 파워 유저 객체를 생성 (Daily, 랭크는 1, 2)
    PowerUserDto user1 = powerUserDto("user1", PeriodType.DAILY, createdAt1, 1L, 33.0, 31.0, 3L, 4L);
    PowerUserDto user2 = powerUserDto("user2", PeriodType.DAILY, createdAt2, 2L, 24.0, 23.0, 1L, 2L);

    //
    when(aggregateSnapshotRepository.findTopByDomainTypeAndPeriodTypeAndStagingTypeOrderByCreatedAtDesc(
        DomainType.POWER_USER, PeriodType.DAILY, StagingType.PUBLISHED))
        .thenReturn(Optional.of(publishedSnapshot(PeriodType.DAILY, DAILY_SNAPSHOT_ID)));

    // 해당 Daily Snapshot에 해당되는 파워 유저는 user1, user2임.
    when(powerUserRepository.findRankingDtosBySnapshotIdAsc(
        DAILY_SNAPSHOT_ID, null, null, PageRequest.of(0, 51)))
        .thenReturn(List.of(user1, user2));

    // 해당 Daily 스냅샷에 해당하는 파워 유저 객체는 2개이므로 count도 2개를 반환하도록 함.
    when(powerUserRepository.countRankingsBySnapshotId(DAILY_SNAPSHOT_ID)).thenReturn(2L);

    // When
    CursorPageResponsePowerUserDto result =
        powerUserService.getLatestRankings(PeriodType.DAILY, DirectionEnum.ASC, null, null, 50);

    // then
    assertEquals(List.of(user1, user2), result.content()); // 결과 CursorPageResponse의 content는 user1, user2
    assertEquals(50, result.size()); // default size는 50임.
    assertEquals(2L, result.totalElements()); // 총 요소의 개수는  user1, user2 -> 2개
    assertFalse(result.hasNext()); // hasNext는 없음
    assertNull(result.nextCursor()); // 첫 페이지라 cursor도 없으며
    assertNull(result.nextAfter()); // 보조 커서(nextAfter)도 없음.

    // 스냅샷 레포지토리에서 DAILY, Published의 가장 최신 PowerUserSnapshot을 찾는 메서드가 실행되었는가?
    verify(aggregateSnapshotRepository)
        .findTopByDomainTypeAndPeriodTypeAndStagingTypeOrderByCreatedAtDesc(
            DomainType.POWER_USER, PeriodType.DAILY, StagingType.PUBLISHED);
    // 파워 유저 레포지토리에서 해당 스냅샷 ID에 해당하는 파워 유저 객체를 랭킹 오름차순으로 조회하는 메서드가 실행되었는가?
    verify(powerUserRepository)
        .findRankingDtosBySnapshotIdAsc(DAILY_SNAPSHOT_ID, null, null, PageRequest.of(0, 51));

    // 파워 유저 레포지토리에서 해당 스냅샷 ID에 해당되는 파워 유저 개수를 반환하는 메서드가 실행되었는가?
    verify(powerUserRepository).countRankingsBySnapshotId(DAILY_SNAPSHOT_ID);
  }

  @Test
  @DisplayName("Publish 된 스냅샷을 가진 PowerUser를 Rank 기준 내림차순으로 조회 (첫 페이지, hasNext = false)" )
  void getLatestRankings_firstPageDesc() {
    // given
    LocalDateTime createdAt1 = LocalDateTime.of(2026, 4, 15, 10, 0);
    LocalDateTime createdAt2 = LocalDateTime.of(2026, 4, 15, 10, 1);

    PowerUserDto user1 = powerUserDto("user1", PeriodType.DAILY, createdAt1, 1L, 33.0, 31.0, 3L, 4L);
    PowerUserDto user2 = powerUserDto("user2", PeriodType.DAILY, createdAt2, 2L, 24.0, 23.0, 1L, 2L);

    when(aggregateSnapshotRepository.findTopByDomainTypeAndPeriodTypeAndStagingTypeOrderByCreatedAtDesc(
        DomainType.POWER_USER, PeriodType.DAILY, StagingType.PUBLISHED))
        .thenReturn(Optional.of(publishedSnapshot(PeriodType.DAILY, DAILY_SNAPSHOT_ID)));
    when(powerUserRepository.findRankingDtosBySnapshotIdDesc(
        DAILY_SNAPSHOT_ID, null, null, PageRequest.of(0, 51)))
        .thenReturn(List.of(user2, user1));
    when(powerUserRepository.countRankingsBySnapshotId(DAILY_SNAPSHOT_ID)).thenReturn(2L);

    // when
    CursorPageResponsePowerUserDto result =
        powerUserService.getLatestRankings(PeriodType.DAILY, DirectionEnum.DESC, null, null, 50);

    // then
    assertEquals(List.of(user2, user1), result.content());
    assertEquals(50, result.size());
    assertEquals(2L, result.totalElements());
    assertFalse(result.hasNext());
    assertNull(result.nextCursor());
    assertNull(result.nextAfter());

    verify(aggregateSnapshotRepository)
        .findTopByDomainTypeAndPeriodTypeAndStagingTypeOrderByCreatedAtDesc(
            DomainType.POWER_USER, PeriodType.DAILY, StagingType.PUBLISHED);
    verify(powerUserRepository)
        .findRankingDtosBySnapshotIdDesc(DAILY_SNAPSHOT_ID, null, null, PageRequest.of(0, 51));
    verify(powerUserRepository).countRankingsBySnapshotId(DAILY_SNAPSHOT_ID);
  }

  @Test
  @DisplayName("중간 페이지의 파워 유저를 조회할 때 (hasNext = true)")
  void getLatestRankings_returnsNextCursor() {
    // Given
    LocalDateTime createdAt1 = LocalDateTime.of(2026, 4, 15, 10, 0);
    LocalDateTime createdAt2 = LocalDateTime.of(2026, 4, 15, 10, 1);
    LocalDateTime createdAt3 = LocalDateTime.of(2026, 4, 15, 10, 2);

    // 파워 유저 객체 1,2,3 생성
    PowerUserDto user1 = powerUserDto("user1", PeriodType.DAILY, createdAt1, 1L, 10.0, 8.0, 1L, 1L);
    PowerUserDto user2 = powerUserDto("user2", PeriodType.DAILY, createdAt2, 2L, 9.0, 7.0, 1L, 1L);
    PowerUserDto user3 = powerUserDto("user3", PeriodType.DAILY, createdAt3, 3L, 8.0, 6.0, 1L, 1L);

    // Daily, Publish 속성의 가장 최신의 snapshot 객체를 호출할 때 임의의 Publish 된 스냅샷 객체를 리턴함.
    when(aggregateSnapshotRepository.findTopByDomainTypeAndPeriodTypeAndStagingTypeOrderByCreatedAtDesc(
        DomainType.POWER_USER, PeriodType.DAILY, StagingType.PUBLISHED))
        .thenReturn(Optional.of(publishedSnapshot(PeriodType.DAILY, DAILY_SNAPSHOT_ID)));

    // 해당 SnapshotId 객체를 가진 파워 유저중에서 랭킹 순으로 오름차순 조회하는 메서드를 호출할 때,
    // user1, user2, user3 파워 객체를 리턴함.
    when(powerUserRepository.findRankingDtosBySnapshotIdAsc(
        DAILY_SNAPSHOT_ID, null, null, PageRequest.of(0, 3)))
        .thenReturn(List.of(user1, user2, user3));

    // 해당 스냅샷을 가진 파워 유저의 개수를 카운트하는 메서드 호출 시,
    // 3을 리턴함.
    when(powerUserRepository.countRankingsBySnapshotId(DAILY_SNAPSHOT_ID)).thenReturn(3L);

    // when
    // 페이지의 최대 size는 2, PowerUser는 3, hasNext = True
    CursorPageResponsePowerUserDto result =
        powerUserService.getLatestRankings(PeriodType.DAILY, DirectionEnum.ASC, null, null, 2);

    // then
    assertTrue(result.hasNext());
    assertEquals(List.of(user1, user2), result.content());
    assertEquals("2", result.nextCursor());
    assertEquals(createdAt2.toString(), result.nextAfter());
    assertEquals(2, result.size());
    assertEquals(3L, result.totalElements());
  }

  @Test
  @DisplayName("커서 형식이 옳바르지 않을 때 예외 던지는 테스트")
  void getLatestRankings_invalidCursor() {
    // given
    DeokhugamException exception =
        assertThrows(
            DeokhugamException.class,
            () ->
                powerUserService.getLatestRankings(
                    PeriodType.DAILY,
                    DirectionEnum.DESC,
                    "invalid-cursor",
                    "2026-04-15T10:00:00",
                    50));

    // when + then
    assertEquals(ErrorCode.CURSOR_OR_AFTER_FORMAT_NOT_VALID, exception.getErrorCode());
    verifyNoInteractions(powerUserRepository, aggregateSnapshotRepository);
  }

  @Test
  @DisplayName("보조 커서(after) 형식이 유효하지 않을 때")
  void getLatestRankings_invalidAfter() {
    // given
    DeokhugamException exception =
        assertThrows(
            DeokhugamException.class,
            () ->
                powerUserService.getLatestRankings(
                    PeriodType.DAILY, DirectionEnum.DESC, "3", "not-a-date", 50));

    // when + then
    assertEquals(ErrorCode.CURSOR_OR_AFTER_FORMAT_NOT_VALID, exception.getErrorCode());
    verifyNoInteractions(powerUserRepository, aggregateSnapshotRepository);
  }

  @Test
  @DisplayName("커서와 보조커서가 함께 제공되지 않을 때")
  void getLatestRankings_cursorAfterMismatch() {
    // given
    DeokhugamException exception =
        assertThrows(
            DeokhugamException.class,
            () ->
                powerUserService.getLatestRankings(
                    PeriodType.DAILY, DirectionEnum.DESC, "3", null, 50));

    // when + then
    assertEquals(ErrorCode.CURSOR_AFTER_NOT_PROVIDED_TOGETHER, exception.getErrorCode());
    verifyNoInteractions(powerUserRepository, aggregateSnapshotRepository);
  }

  @Test
  @DisplayName("스냅샷을 찾을 수 없을 때")
  void getLatestRankings_snapshotNotFound() {
    // given
    // 스냅샷 레포지토리에서 해당 PeriodType을 가진 publish된 스냅샷을 찾지 못함
    when(aggregateSnapshotRepository.findTopByDomainTypeAndPeriodTypeAndStagingTypeOrderByCreatedAtDesc(
        DomainType.POWER_USER, PeriodType.MONTHLY, StagingType.PUBLISHED))
        .thenReturn(Optional.empty());

    DeokhugamException exception =
        assertThrows(
            DeokhugamException.class,
            () ->
                powerUserService.getLatestRankings(
                    PeriodType.MONTHLY, DirectionEnum.ASC, null, null, 20));

    // when + then
    assertEquals(ErrorCode.SNAPSHOT_NOT_FOUND, exception.getErrorCode());
    verify(aggregateSnapshotRepository)
        .findTopByDomainTypeAndPeriodTypeAndStagingTypeOrderByCreatedAtDesc(
            DomainType.POWER_USER, PeriodType.MONTHLY, StagingType.PUBLISHED);
    verifyNoInteractions(powerUserRepository);
  }


  // DAILY_SNAPSHOT_ID를 가지는 PUBLISH 된 임의의 스냅샷 객체를 생성하는 메서드
  private AggregateSnapshot publishedSnapshot(PeriodType periodType, UUID snapshotId) {
    return AggregateSnapshot.builder()
        .snapshotId(snapshotId)
        .domainType(DomainType.POWER_USER)
        .periodType(periodType)
        .aggregatedAt(LocalDateTime.of(2026, 4, 15, 0, 0))
        .stagingType(StagingType.PUBLISHED)
        .build();
  }

  // PowerUser 생성 메서드
  private PowerUserDto powerUserDto(
      String nickname,
      PeriodType periodType,
      LocalDateTime createdAt,
      long rank,
      double score,
      double reviewScoreSum,
      long likeCount,
      long commentCount) {
    return new PowerUserDto(
        UUID.randomUUID(),
        nickname,
        periodType,
        createdAt,
        rank,
        score,
        reviewScoreSum,
        likeCount,
        commentCount);
  }
}
