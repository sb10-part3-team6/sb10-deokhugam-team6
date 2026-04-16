package com.codeit.mission.deokhugam.dashboard.users.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeit.mission.deokhugam.dashboard.DirectionEnum;
import com.codeit.mission.deokhugam.dashboard.PeriodType;
import com.codeit.mission.deokhugam.dashboard.users.dto.CursorPageResponsePowerUserDto;
import com.codeit.mission.deokhugam.dashboard.users.dto.PowerUserDto;
import com.codeit.mission.deokhugam.dashboard.users.repository.PowerUserRepository;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class PowerUserServiceTest {

  @Mock
  PowerUserRepository powerUserRepository;

  @InjectMocks
  PowerUserService powerUserService;

  @Test
  @DisplayName("파워 유저 테이블에서 일간 파워 유저 조회 성공 케이스")
  void get_daily_power_user_success() {
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
  @DisplayName("파워 유저 테이블에서 일간 파워 유저 조회 실패 케이스")
  void get_daily_power_user_fail(){

  }
}