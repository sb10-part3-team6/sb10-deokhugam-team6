package com.codeit.mission.deokhugam.dashboard.users.service;

import com.codeit.mission.deokhugam.dashboard.DirectionEnum;
import com.codeit.mission.deokhugam.dashboard.PeriodType;
import com.codeit.mission.deokhugam.dashboard.StagingType;
import com.codeit.mission.deokhugam.dashboard.users.dto.CursorPageResponsePowerUserDto;
import com.codeit.mission.deokhugam.dashboard.users.dto.PowerUserDto;
import com.codeit.mission.deokhugam.dashboard.users.entity.PowerUserSnapshot;
import com.codeit.mission.deokhugam.dashboard.users.exception.InvalidCursorValueException;
import com.codeit.mission.deokhugam.dashboard.users.exception.SnapshotNotFoundException;
import com.codeit.mission.deokhugam.dashboard.users.repository.PowerUserRepository;
import com.codeit.mission.deokhugam.dashboard.users.repository.PowerUserSnapshotRepository;
import com.codeit.mission.deokhugam.error.DeokhugamException;
import com.codeit.mission.deokhugam.error.ErrorCode;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PowerUserService {
  private static final int MAX_PAGE_SIZE = 100;

  private final PowerUserRepository powerUserRepository;
  private final PowerUserSnapshotRepository powerUserSnapshotRepository;

  @Transactional(readOnly = true)
  public CursorPageResponsePowerUserDto getLatestRankings(
      PeriodType periodType, DirectionEnum direction, String cursor, String after, int size) {
    if ((cursor == null) != (after == null)) {
      throw new DeokhugamException(ErrorCode.CURSOR_AFTER_NOT_PROVIDED_TOGETHER);
    }

    int pageSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);

    Long cursorLong = null;
    LocalDateTime afterDate = null;

    try {
      if (cursor != null) {
        cursorLong = Long.parseLong(cursor);
        afterDate = LocalDateTime.parse(after);
      }
    } catch (NumberFormatException | DateTimeException e) {
      throw new InvalidCursorValueException();
    }

    UUID publishedSnapshotId = getPublishedSnapshotId(periodType);

    List<PowerUserDto> rows = new ArrayList<>();
    if (direction == DirectionEnum.ASC) {
      rows = powerUserRepository.findRankingDtosBySnapshotIdAsc(
          publishedSnapshotId, cursorLong, afterDate, PageRequest.of(0, pageSize + 1));
    } else {
      rows = powerUserRepository.findRankingDtosBySnapshotIdDesc(
          publishedSnapshotId, cursorLong, afterDate, PageRequest.of(0, pageSize + 1));
    }

    boolean hasNext = rows.size() > pageSize;
    List<PowerUserDto> content = hasNext ? new ArrayList<>(rows.subList(0, pageSize)) : rows;

    String nextCursor = null;
    String nextAfter = null;
    if (hasNext && !content.isEmpty()) {
      PowerUserDto last = content.get(content.size() - 1);
      nextCursor = String.valueOf(last.rank());
      nextAfter = last.createdAt().toString();
    }

    long totalElements = powerUserRepository.countRankingsBySnapshotId(publishedSnapshotId);

    return new CursorPageResponsePowerUserDto(
        content,
        nextCursor,
        nextAfter,
        pageSize,
        totalElements,
        hasNext);
  }

  private UUID getPublishedSnapshotId(PeriodType periodType) {
    return powerUserSnapshotRepository
        .findTopByPeriodTypeAndStagingTypeOrderByCreatedAtDesc(periodType, StagingType.PUBLISHED)
        .map(PowerUserSnapshot::getSnapshotId)
        .orElseThrow(SnapshotNotFoundException::new);
  }
}
