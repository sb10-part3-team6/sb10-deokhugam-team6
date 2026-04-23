package com.codeit.mission.deokhugam.dashboard.popularreviews.service;

import com.codeit.mission.deokhugam.dashboard.DirectionEnum;
import com.codeit.mission.deokhugam.dashboard.DomainType;
import com.codeit.mission.deokhugam.dashboard.PeriodType;
import com.codeit.mission.deokhugam.dashboard.StagingType;
import com.codeit.mission.deokhugam.dashboard.exceptions.CursorAfterNotProvidedTogetherException;
import com.codeit.mission.deokhugam.dashboard.exceptions.InvalidCursorValueException;
import com.codeit.mission.deokhugam.dashboard.exceptions.SnapshotNotFoundException;
import com.codeit.mission.deokhugam.dashboard.popularreviews.dto.CursorPageResponsePopularReviewDto;
import com.codeit.mission.deokhugam.dashboard.popularreviews.dto.PopularReviewDto;
import com.codeit.mission.deokhugam.dashboard.popularreviews.repository.PopularReviewRepository;
import com.codeit.mission.deokhugam.dashboard.snapshot.AggregateSnapshot;
import com.codeit.mission.deokhugam.dashboard.snapshot.AggregateSnapshotRepository;
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
public class PopularReviewService {
  private static final int MAX_PAGE_SIZE = 100;

  private final PopularReviewRepository popularReviewRepository;
  private final AggregateSnapshotRepository aggregateSnapshotRepository;

  // 인기 리뷰를 조회하는 서비스 메서드
  @Transactional(readOnly = true)
  public CursorPageResponsePopularReviewDto getReviews(
      PeriodType periodType, DirectionEnum direction, String cursor, String after, int size) {

    // 커서와 보조 커서는 항상 같이 제공되어야 합니다.
    if ((cursor == null) != (after == null)) {
      throw new CursorAfterNotProvidedTogetherException();
    }

    // 페이지 사이즈는 1 ~ 100 범위를 벗어나지 않음.
    int pageSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);

    // 커서와 보조 커서 null로 초기화
    Long cursorLong = null;
    LocalDateTime afterDate = null;

    try {
      if (cursor != null) {
        // 커서 값이 존재하면, 커서와 보조 커서를 페이징을 위해 Long,LocalDateTime으로 파싱
        cursorLong = Long.parseLong(cursor);
        afterDate = LocalDateTime.parse(after);
      }
    } catch (NumberFormatException | DateTimeException e) {
      throw new InvalidCursorValueException();
    }

    // 찾고자하는 기간에 해당하는 스냅샷의 ID를 구합니다.
    UUID publishedSnapshotId = getPublishedSnapshotId(periodType);

    // 커서 페이지 응답에 content 안에 들어갈 PopularReviewDto 리스트
    List<PopularReviewDto> rows = new ArrayList<>();
    if (direction == DirectionEnum.ASC) { // 오름차순
      rows =
          // 레포지토리에서 오름차 순 정렬하여 뽑아옴
          popularReviewRepository.findRankingDtosBySnapshotIdAsc(
              publishedSnapshotId, cursorLong, afterDate, PageRequest.of(0, pageSize + 1));
    } else {
      rows =
          // 내림차순
          popularReviewRepository.findRankingDtosBySnapshotIdDesc(
              publishedSnapshotId, cursorLong, afterDate, PageRequest.of(0, pageSize + 1));
    }

    // 뽑아온 rows의 사이즈가 한 페이지의 사이즈보다 크면 다음 페이지가 존재합니다.
    boolean hasNext = rows.size() > pageSize;
    // 다음 페이지가 존재하면 rows에서 페이지 사이즈 만큼 뽑아오고,
    // 존재하지 않으면 rows를 통째로 뽑습니다.
    List<PopularReviewDto> content = hasNext ? new ArrayList<>(rows.subList(0, pageSize)) : rows;

    // 다음 페이지를 조회하기 위한 Cursor와 보조 커서
    String nextCursor = null;
    String nextAfter = null;
    // 다음 페이지가 존재하고 콘텐츠가 존재
    if (hasNext && !content.isEmpty()) {
      PopularReviewDto last = content.get(content.size() - 1);
      // 컨텐츠 내 마지막 요소를 기준으로 next cursor와 보조 커서를 초기화한다.
      nextCursor = String.valueOf(last.rank());
      nextAfter = last.createdAt().toString();
    }

    // 스냅샷에 해당하는 요소들의 총 개수를 센다.
    long totalElements = popularReviewRepository.countRankingsBySnapshotId(publishedSnapshotId);

    return new CursorPageResponsePopularReviewDto(
        content,
        nextCursor,
        nextAfter,
        pageSize,
        totalElements,
        hasNext);
  }

  // periodType과 DomainType=Book으로 snapshotid를 뽑아옵니다.
  private UUID getPublishedSnapshotId(PeriodType periodType) {
    return aggregateSnapshotRepository
        .findTopByDomainTypeAndPeriodTypeAndStagingTypeOrderByCreatedAtDesc(
            DomainType.POPULAR_REVIEW, periodType, StagingType.PUBLISHED)
        .map(AggregateSnapshot::getSnapshotId)
        .orElseThrow(SnapshotNotFoundException::new);
  }
}
