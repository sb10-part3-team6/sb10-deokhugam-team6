package com.codeit.mission.deokhugam.dashboard.users.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeit.mission.deokhugam.dashboard.DirectionEnum;
import com.codeit.mission.deokhugam.dashboard.PeriodType;
import com.codeit.mission.deokhugam.dashboard.users.dto.CursorPageResponsePowerUserDto;
import com.codeit.mission.deokhugam.dashboard.users.dto.PowerUserDto;
import com.codeit.mission.deokhugam.dashboard.users.repository.PowerUserRepository;
import com.codeit.mission.deokhugam.error.DeokhugamException;
import java.time.LocalDateTime;
import java.util.List;
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

  @Mock
  PowerUserRepository powerUserRepository;

  @InjectMocks
  PowerUserService powerUserService;

  @Test
  @DisplayName("일간 파워 유저 첫 페이지 조회 성공 (ASC)")
  void getDailyPowerUserFirstPageAsc() {
    // GIVEN

    LocalDateTime createdAt1 = LocalDateTime.of(2026, 4, 15, 10, 0);
    LocalDateTime createdAt2 = LocalDateTime.of(2026, 4, 15, 10, 1);

    PowerUserDto user1 = new PowerUserDto(
        UUID.fromString("11111111-1111-1111-1111-111111111111"),
        "user1",
        PeriodType.DAILY,
        createdAt1,
        1L,
        33.0,
        31.0,
        3L,
        4L
    );

    PowerUserDto user2 = new PowerUserDto(
        UUID.fromString("22222222-1111-1111-1111-111111111111"),
        "user2",
        PeriodType.DAILY,
        createdAt2,
        2L,
        24.0,
        23.0,
        1L,
        2L
    );

    when(powerUserRepository.findLatestRankingDtosByPeriodTypeAsc(PeriodType.DAILY, null, null,
        PageRequest.of(0, 50 + 1)))
        .thenReturn(List.of(user1, user2));

    when(powerUserRepository.countLatestRankingsByPeriodType(PeriodType.DAILY)).thenReturn(2L);

    // when
    CursorPageResponsePowerUserDto result =
    powerUserService.getLatestRankings(PeriodType.DAILY, DirectionEnum.ASC, null, null, 50);

    // then
    assertEquals(2, result.content().size());
    assertEquals(user1, result.content().get(0));
    assertEquals(user2, result.content().get(1));
    assertEquals(50, result.size());
    assertEquals(2L, result.totalElements());
    assertFalse(result.hasNext());
    assertNull(result.nextCursor());
    assertNull(result.nextAfter());

    verify(powerUserRepository).findLatestRankingDtosByPeriodTypeAsc(
        PeriodType.DAILY,
        null,
        null,
        PageRequest.of(0, 51)
    );
    verify(powerUserRepository).countLatestRankingsByPeriodType(PeriodType.DAILY);
  }

  @Test
  @DisplayName("일간 파워 유저 첫 페이지 조회 성공 (DESC)")
  void getDailyPowerUserFirstPageDesc() {
    // GIVEN

    LocalDateTime createdAt1 = LocalDateTime.of(2026, 4, 15, 10, 0);
    LocalDateTime createdAt2 = LocalDateTime.of(2026, 4, 15, 10, 1);

    PowerUserDto user1 = new PowerUserDto(
        UUID.fromString("11111111-1111-1111-1111-111111111111"),
        "user1",
        PeriodType.DAILY,
        createdAt1,
        1L,
        33.0,
        31.0,
        3L,
        4L
    );

    PowerUserDto user2 = new PowerUserDto(
        UUID.fromString("22222222-1111-1111-1111-111111111111"),
        "user2",
        PeriodType.DAILY,
        createdAt2,
        2L,
        24.0,
        23.0,
        1L,
        2L
    );

    when(powerUserRepository.findLatestRankingDtosByPeriodTypeDesc(PeriodType.DAILY, null, null,
        PageRequest.of(0, 50 + 1)))
        .thenReturn(List.of(user2, user1));

    when(powerUserRepository.countLatestRankingsByPeriodType(PeriodType.DAILY)).thenReturn(2L);

    // when
    CursorPageResponsePowerUserDto result =
        powerUserService.getLatestRankings(PeriodType.DAILY, DirectionEnum.DESC, null, null, 50);

    // then
    assertEquals(2, result.content().size());
    assertEquals(user2, result.content().get(0));
    assertEquals(user1, result.content().get(1));
    assertEquals(50, result.size());
    assertEquals(2L, result.totalElements());
    assertFalse(result.hasNext());
    assertNull(result.nextCursor());
    assertNull(result.nextAfter());

    verify(powerUserRepository).findLatestRankingDtosByPeriodTypeDesc(
        PeriodType.DAILY,
        null,
        null,
        PageRequest.of(0, 51)
    );
    verify(powerUserRepository).countLatestRankingsByPeriodType(PeriodType.DAILY);
  }

  @Test
  @DisplayName("주간 파워 유저 첫 페이지 조회 성공")
  void getWeeklyPowerUsersFirstPage() {
    // GIVEN
    LocalDateTime createdAt1 = LocalDateTime.of(2026, 4, 14, 0, 0);
    LocalDateTime createdAt2 = LocalDateTime.of(2026, 4, 14, 0, 1);

    PowerUserDto user1 = new PowerUserDto(
        UUID.fromString("11111111-1111-1111-1111-111111111111"),
        "weekly-user1",
        PeriodType.WEEKLY,
        createdAt1,
        1L,
        40.0,
        35.0,
        3L,
        2L
    );

    PowerUserDto user2 = new PowerUserDto(
        UUID.fromString("22222222-2222-2222-2222-222222222222"),
        "weekly-user2",
        PeriodType.WEEKLY,
        createdAt2,
        2L,
        30.0,
        25.0,
        2L,
        1L
    );

    when(powerUserRepository.findLatestRankingDtosByPeriodTypeDesc(
        PeriodType.WEEKLY,
        null,
        null,
        PageRequest.of(0, 51)
    )).thenReturn(List.of(user1, user2));

    when(powerUserRepository.countLatestRankingsByPeriodType(PeriodType.WEEKLY))
        .thenReturn(2L);

    // WHEN
    CursorPageResponsePowerUserDto result =
        powerUserService.getLatestRankings(
            PeriodType.WEEKLY,
            DirectionEnum.DESC,
            null,
            null,
            50
        );

    // THEN
    assertEquals(2, result.content().size());
    assertEquals(user1, result.content().get(0));
    assertEquals(user2, result.content().get(1));
    assertEquals(2L, result.totalElements());
    assertFalse(result.hasNext());
    assertNull(result.nextCursor());
    assertNull(result.nextAfter());
  }

  @Test
  @DisplayName("월간 파워 유저 첫 페이지 조회 성공")
  void getMonthlyPowerUsersFirstPage() {
    LocalDateTime createdAt = LocalDateTime.of(2026, 4, 1, 0, 0);

    PowerUserDto user1 = new PowerUserDto(
        UUID.fromString("33333333-3333-3333-3333-333333333333"),
        "monthly-user1",
        PeriodType.MONTHLY,
        createdAt,
        1L,
        55.0,
        45.0,
        5L,
        3L
    );

    when(powerUserRepository.findLatestRankingDtosByPeriodTypeDesc(
        PeriodType.MONTHLY,
        null,
        null,
        PageRequest.of(0, 51)
    )).thenReturn(List.of(user1));

    when(powerUserRepository.countLatestRankingsByPeriodType(PeriodType.MONTHLY))
        .thenReturn(1L);

    CursorPageResponsePowerUserDto result =
        powerUserService.getLatestRankings(
            PeriodType.MONTHLY,
            DirectionEnum.DESC,
            null,
            null,
            50
        );

    assertEquals(1, result.content().size());
    assertEquals(user1, result.content().get(0));
    assertEquals(1L, result.totalElements());
    assertFalse(result.hasNext());
  }

  @Test
  @DisplayName("상시 파워 유저 첫 페이지 조회 성공")
  void getAllTimePowerUsersFirstPage() {
    LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 0, 0);

    PowerUserDto user1 = new PowerUserDto(
        UUID.fromString("44444444-4444-4444-4444-444444444444"),
        "alltime-user1",
        PeriodType.ALL_TIME,
        createdAt,
        1L,
        100.0,
        80.0,
        10L,
        8L
    );

    when(powerUserRepository.findLatestRankingDtosByPeriodTypeDesc(
        PeriodType.ALL_TIME,
        null,
        null,
        PageRequest.of(0, 51)
    )).thenReturn(List.of(user1));

    when(powerUserRepository.countLatestRankingsByPeriodType(PeriodType.ALL_TIME))
        .thenReturn(1L);

    CursorPageResponsePowerUserDto result =
        powerUserService.getLatestRankings(
            PeriodType.ALL_TIME,
            DirectionEnum.DESC,
            null,
            null,
            50
        );

    assertEquals(1, result.content().size());
    assertEquals(user1, result.content().get(0));
    assertEquals(1L, result.totalElements());
    assertFalse(result.hasNext());
  }

  @Test
  @DisplayName("일간 파워 유저 중간 페이지 조회 성공")
  void getDailyPowerUsersMiddlePage(){
    // Given
    LocalDateTime createdAt1 = LocalDateTime.of(2026, 4, 15, 10, 0);
    LocalDateTime createdAt2 = LocalDateTime.of(2026, 4, 15, 10, 1);
    LocalDateTime createdAt3 = LocalDateTime.of(2026, 4, 15, 10, 2);

    PowerUserDto user1 = new PowerUserDto(
        UUID.fromString("11111111-1111-1111-1111-111111111111"),
        "user1", PeriodType.DAILY, createdAt1, 1L, 10.0, 8.0, 1L, 1L
    );
    PowerUserDto user2 = new PowerUserDto(
        UUID.fromString("22222222-2222-2222-2222-222222222222"),
        "user2", PeriodType.DAILY, createdAt2, 2L, 9.0, 7.0, 1L, 1L
    );
    PowerUserDto user3 = new PowerUserDto(
        UUID.fromString("33333333-3333-3333-3333-333333333333"),
        "user3", PeriodType.DAILY, createdAt3, 3L, 8.0, 6.0, 1L, 1L
    );

    when(powerUserRepository.findLatestRankingDtosByPeriodTypeAsc(
        PeriodType.DAILY, null, null, PageRequest.of(0, 3)
    )).thenReturn(List.of(user1, user2, user3));

    when(powerUserRepository.countLatestRankingsByPeriodType(PeriodType.DAILY))
        .thenReturn(3L);

    CursorPageResponsePowerUserDto result =
        powerUserService.getLatestRankings(
            PeriodType.DAILY,
            DirectionEnum.ASC,
            null,
            null,
            2
        );

    assertTrue(result.hasNext());
    assertEquals(2, result.content().size());
    assertEquals(user1, result.content().get(0));
    assertEquals(user2, result.content().get(1));
    assertEquals("2", result.nextCursor());
    assertEquals(createdAt2.toString(), result.nextAfter());
  }

  @Test
  @DisplayName("잘못된 cursor 값 입력 시 예외가 발생한다")
  void getDailyPowerUsersWrongCursor() {
    assertThrows(DeokhugamException.class, () ->
        powerUserService.getLatestRankings(
            PeriodType.DAILY,
            DirectionEnum.DESC,
            "sfsfsfe",
            null,
            50
        )
    );
  }

  @Test
  @DisplayName("잘못된 after 값 입력 시 커스텀 예외가 발생한다")
  void getDailyPowerUsersWrongNext(){
    assertThrows(DeokhugamException.class, () ->
        powerUserService.getLatestRankings(
            PeriodType.DAILY,
            DirectionEnum.DESC,
            "33333333-3333-3333-3333-333333333333",
            "1231",
            50
        )
    );
  }




}