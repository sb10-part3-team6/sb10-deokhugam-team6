package com.codeit.mission.deokhugam.dashboard.users.controller;

import com.codeit.mission.deokhugam.dashboard.DirectionEnum;
import com.codeit.mission.deokhugam.dashboard.PeriodType;
import com.codeit.mission.deokhugam.dashboard.users.dto.CursorPageResponsePowerUserDto;
import com.codeit.mission.deokhugam.dashboard.users.service.PowerUserService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/power")
@RequiredArgsConstructor
public class PowerUserController {


  private final PowerUserService powerUserService;

  // 집계된 파워 유저의 DTO를 목록화해서 응답함
  // 파라미터로 기간, cursor, size가 들어감
  // cursor를 공백으로 둘 시 첫 페이지 반환되고 size는 기본값이 50
  @GetMapping
  public ResponseEntity<CursorPageResponsePowerUserDto> getPowerUsers(
      @RequestParam(defaultValue = "DAILY") PeriodType period, // 기간 (일간, 주간, 월간, 올타임)
      @RequestParam(defaultValue = "DESC") DirectionEnum direction, // 정렬 방향
      @RequestParam(required = false) String cursor, // 커서는 필수 값이 아님.
      @RequestParam(required = false) String after,
      @RequestParam(defaultValue = "50") int size){ // 페이지의 사이즈는 기본값이 50

    // ResponseEntity는 ok를 보내고 body값으로 slice된 PowerUserDto 들을 반환함.
    return ResponseEntity.ok(powerUserService.getLatestRankings(period, direction, cursor, after, size));
  }
}
