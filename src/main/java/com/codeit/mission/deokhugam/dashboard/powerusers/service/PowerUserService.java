package com.codeit.mission.deokhugam.dashboard.powerusers.service;

import com.codeit.mission.deokhugam.dashboard.DirectionEnum;
import com.codeit.mission.deokhugam.dashboard.DomainType;
import com.codeit.mission.deokhugam.dashboard.PeriodType;
import com.codeit.mission.deokhugam.dashboard.StagingType;
import com.codeit.mission.deokhugam.dashboard.snapshot.AggregateSnapshot;
import com.codeit.mission.deokhugam.dashboard.snapshot.AggregateSnapshotRepository;
import com.codeit.mission.deokhugam.dashboard.powerusers.dto.CursorPageResponsePowerUserDto;
import com.codeit.mission.deokhugam.dashboard.powerusers.dto.PowerUserDto;
import com.codeit.mission.deokhugam.dashboard.exceptions.CursorAfterNotProvidedTogetherException;
import com.codeit.mission.deokhugam.dashboard.exceptions.InvalidCursorValueException;
import com.codeit.mission.deokhugam.dashboard.powerusers.repository.PowerUserRepository;
import java.time.DateTimeException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
  private final AggregateSnapshotRepository aggregateSnapshotRepository;

  @Transactional(readOnly = true)
  public CursorPageResponsePowerUserDto getLatestRankings(
      PeriodType periodType, DirectionEnum direction, String cursor, String after, int size) {
    if ((cursor == null) != (after == null)) {
      throw new CursorAfterNotProvidedTogetherException();
    }

    int pageSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);

    Long cursorLong = null;
    Instant afterDate = null;

    try {
      if (cursor != null) {
        cursorLong = Long.parseLong(cursor);
        afterDate = Instant.parse(after);
      }
    } catch (NumberFormatException | DateTimeException e) {
      throw new InvalidCursorValueException();
    }

    Optional<UUID> publishedSnapshotId = getPublishedSnapshotId(periodType);
    if (publishedSnapshotId.isEmpty()) {
      return new CursorPageResponsePowerUserDto(
          Collections.emptyList(),
          null,
          null,
          pageSize,
          0L,
          false);
    }

    List<PowerUserDto> rows = new ArrayList<>();
    if (direction == DirectionEnum.ASC) {
      rows = powerUserRepository.findRankingDtosBySnapshotIdAsc(
          publishedSnapshotId.get(), cursorLong, afterDate, PageRequest.of(0, pageSize + 1));
    } else {
      rows = powerUserRepository.findRankingDtosBySnapshotIdDesc(
          publishedSnapshotId.get(), cursorLong, afterDate, PageRequest.of(0, pageSize + 1));
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

    long totalElements = powerUserRepository.countRankingsBySnapshotId(publishedSnapshotId.get());

    return new CursorPageResponsePowerUserDto(
        content,
        nextCursor,
        nextAfter,
        pageSize,
        totalElements,
        hasNext);
  }

  private Optional<UUID> getPublishedSnapshotId(PeriodType periodType) {
    return aggregateSnapshotRepository
        .findTopByDomainTypeAndPeriodTypeAndStagingTypeOrderByCreatedAtDesc(
            DomainType.POWER_USER, periodType, StagingType.PUBLISHED)
        .map(AggregateSnapshot::getSnapshotId);
  }
}
