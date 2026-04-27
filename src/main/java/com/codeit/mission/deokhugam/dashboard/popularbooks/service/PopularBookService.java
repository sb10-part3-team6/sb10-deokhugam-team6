package com.codeit.mission.deokhugam.dashboard.popularbooks.service;

import com.codeit.mission.deokhugam.dashboard.DirectionEnum;
import com.codeit.mission.deokhugam.dashboard.DomainType;
import com.codeit.mission.deokhugam.dashboard.PeriodType;
import com.codeit.mission.deokhugam.dashboard.StagingType;
import com.codeit.mission.deokhugam.dashboard.dto.ParsedCursors;
import com.codeit.mission.deokhugam.dashboard.dto.StringCursors;
import com.codeit.mission.deokhugam.dashboard.exceptions.CursorAfterNotProvidedTogetherException;
import com.codeit.mission.deokhugam.dashboard.exceptions.SnapshotNotFoundException;
import com.codeit.mission.deokhugam.dashboard.popularbooks.dto.CursorPageResponsePopularBookDto;
import com.codeit.mission.deokhugam.dashboard.popularbooks.dto.PopularBookDto;
import com.codeit.mission.deokhugam.dashboard.popularbooks.repository.PopularBookRepository;
import com.codeit.mission.deokhugam.dashboard.snapshot.AggregateSnapshot;
import com.codeit.mission.deokhugam.dashboard.snapshot.AggregateSnapshotRepository;
import com.codeit.mission.deokhugam.dashboard.util.Utils;
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
public class PopularBookService {
  private static final int MAX_PAGE_SIZE = 100; // 페이지의 최대 사이즈는 100으로 고정

  private final PopularBookRepository popularBookRepository;
  private final AggregateSnapshotRepository aggregateSnapshotRepository;

  // 인기 도서를 조회하는 서비스 계층 메서드
  @Transactional(readOnly = true)
  public CursorPageResponsePopularBookDto get(
      PeriodType periodType, DirectionEnum direction, String cursor, String after, int size
  ){

    // 커서와 보조 커서는 항상 같이 제공되어야 합니다.
    if ((cursor == null) != (after == null)) {
      throw new CursorAfterNotProvidedTogetherException();
    }

    // 페이지 사이즈는 1 ~ 100 범위를 벗어나지 않음.
    int pageSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);

    // 커서가 존재하면 String으로 부터 Long,LocalDate로 파싱
    // 존재하지 않다면 둘 다 null로 초기화
    ParsedCursors cursors = Utils.parseCursors(cursor, after);
    Long cursorLong = cursors.cursor();
    LocalDateTime afterDate = cursors.after();

    // 찾고자하는 기간에 해당하는 스냅샷의 ID를 구합니다.
    UUID publishedSnapshotId = getPublishedSnapshotId(periodType);

    // 커서 페이지 응답에 content 안에 들어갈 PopularReviewDto 리스트
    List<PopularBookDto> rows = (direction == DirectionEnum.ASC)
        ? popularBookRepository.findRankingDtosBySnapshotIdAsc(publishedSnapshotId, cursorLong, afterDate, PageRequest.of(0, pageSize + 1))
        : popularBookRepository.findRankingDtosBySnapshotIdDesc(publishedSnapshotId, cursorLong, afterDate, PageRequest.of(0, pageSize + 1));

    // 뽑아온 rows의 사이즈가 한 페이지의 사이즈보다 크면 다음 페이지가 존재합니다.
    boolean hasNext = rows.size() > pageSize;
    // 다음 페이지가 존재하면 rows에서 페이지 사이즈 만큼 뽑아오고,
    // 존재하지 않으면 rows를 통째로 뽑습니다.
    List<PopularBookDto> content = hasNext ? new ArrayList<>(rows.subList(0, pageSize)) : rows;

    // 다음 페이지를 조회하기 위한 Cursor와 보조 커서를 구함
    // 다음 페이지가 존재하지 않으면 다음 커서 / 보조 커서는 null 처리
    StringCursors nextCursors = getNextCursors(hasNext, content);
    String nextCursor = nextCursors.cursor();
    String nextAfter = nextCursors.after();

    // 스냅샷에 해당하는 요소들의 총 개수를 센다.
    long totalElements = popularBookRepository.countRankingsBySnapshotId(publishedSnapshotId);

    return new CursorPageResponsePopularBookDto(
        content,
        nextCursor,
        nextAfter,
        pageSize,
        totalElements,
        hasNext);
  }

  // periodType과 DomainType=인기 리뷰으로 snapshotid를 뽑아옵니다.
  private UUID getPublishedSnapshotId(PeriodType periodType) {
    return aggregateSnapshotRepository
        .findTopByDomainTypeAndPeriodTypeAndStagingTypeOrderByCreatedAtDesc(
            DomainType.POPULAR_BOOK, periodType, StagingType.PUBLISHED)
        .map(AggregateSnapshot::getSnapshotId)
        .orElseThrow(SnapshotNotFoundException::new);
  }

  private StringCursors getNextCursors(boolean hasNext, List<PopularBookDto> content){
    if (hasNext && !content.isEmpty()) {
      PopularBookDto last = content.get(content.size() - 1);
      return new StringCursors(String.valueOf(last.rank()), String.valueOf(last.createdAt()));
    }
    return new StringCursors(null, null);
  }

}
