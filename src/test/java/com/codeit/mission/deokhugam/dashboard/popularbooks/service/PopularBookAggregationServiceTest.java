package com.codeit.mission.deokhugam.dashboard.popularbooks.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeit.mission.deokhugam.dashboard.PeriodType;
import com.codeit.mission.deokhugam.dashboard.popularbooks.dto.request.BookReviewAvgRating;
import com.codeit.mission.deokhugam.dashboard.popularbooks.dto.request.BookReviewCount;
import com.codeit.mission.deokhugam.dashboard.popularbooks.dto.request.PopularBookStat;
import com.codeit.mission.deokhugam.dashboard.popularbooks.entity.PopularBook;
import com.codeit.mission.deokhugam.dashboard.popularbooks.repository.PopularBookRepository;
import com.codeit.mission.deokhugam.review.entity.ReviewStatus;
import com.codeit.mission.deokhugam.review.repository.ReviewRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PopularBookAggregationServiceTest {

  @Mock
  private PopularBookRepository popularBookRepository;

  @Mock
  private ReviewRepository reviewRepository;

  @InjectMocks
  private PopularBookAggregationService popularBookAggregationService;

  @Test
  @DisplayName("인기 도서 집계에 필요한 지수들을 일괄 로딩하는 테스트 (성공)")
  void loadBookStat_success() {
    Instant periodStart = Instant.parse("2026-04-16T15:30:00Z");
    Instant periodEnd = Instant.parse("2026-04-23T15:30:00Z");
    UUID higherBookId = UUID.randomUUID();
    UUID lowerBookId = UUID.randomUUID();

    when(reviewRepository.countReviewsPerBook(periodStart, periodEnd, ReviewStatus.ACTIVE))
        .thenReturn(List.of(
            new BookReviewCount(higherBookId, 5L),
            new BookReviewCount(lowerBookId, 1L)));
    when(reviewRepository.avgRatingsPerBook(periodStart, periodEnd, ReviewStatus.ACTIVE))
        .thenReturn(List.of(
            new BookReviewAvgRating(higherBookId, 4.5),
            new BookReviewAvgRating(lowerBookId, 3.0)));

    Map<UUID, PopularBookStat> statsPerBook =
        popularBookAggregationService.loadBookStat(PeriodType.WEEKLY, periodEnd);

    assertEquals(2, statsPerBook.size());
    assertEquals(higherBookId, statsPerBook.get(higherBookId).bookId());
    assertEquals(lowerBookId, statsPerBook.get(lowerBookId).bookId());
    assertEquals(5L, statsPerBook.get(higherBookId).reviewCount());
    assertEquals(4.5, statsPerBook.get(higherBookId).reviewAvgRating());
    assertEquals(1L, statsPerBook.get(lowerBookId).reviewCount());
    assertEquals(3.0, statsPerBook.get(lowerBookId).reviewAvgRating());
    verify(reviewRepository).countReviewsPerBook(periodStart, periodEnd, ReviewStatus.ACTIVE);
    verify(reviewRepository).avgRatingsPerBook(periodStart, periodEnd, ReviewStatus.ACTIVE);
  }

  @Test
  @DisplayName("리뷰 수와 평균 평점 중 하나만 있어도 도서 지수를 생성한다")
  void loadBookStat_mergesBookIdsFromBothSources() {
    Instant aggregatedAt = Instant.parse("2026-04-21T00:00:00Z");
    Instant periodStart = Instant.parse("2026-04-14T00:00:00Z");
    UUID countOnlyBookId = UUID.randomUUID();
    UUID ratingOnlyBookId = UUID.randomUUID();

    when(reviewRepository.countReviewsPerBook(periodStart, aggregatedAt, ReviewStatus.ACTIVE))
        .thenReturn(List.of(new BookReviewCount(countOnlyBookId, 2L)));
    when(reviewRepository.avgRatingsPerBook(periodStart, aggregatedAt, ReviewStatus.ACTIVE))
        .thenReturn(List.of(new BookReviewAvgRating(ratingOnlyBookId, 4.0)));

    Map<UUID, PopularBookStat> statsPerBook =
        popularBookAggregationService.loadBookStat(PeriodType.WEEKLY, aggregatedAt);

    assertEquals(2, statsPerBook.size());
    assertEquals(2L, statsPerBook.get(countOnlyBookId).reviewCount());
    assertEquals(0.0, statsPerBook.get(countOnlyBookId).reviewAvgRating());
    assertEquals(0L, statsPerBook.get(ratingOnlyBookId).reviewCount());
    assertEquals(4.0, statsPerBook.get(ratingOnlyBookId).reviewAvgRating());
  }

  @Test
  @DisplayName("주중 시각을 기준으로 배치 테스트 (성공)")
  void rankPopularBooks_findsRowsBySnapshotId() {
    Instant aggregatedAt = Instant.parse("2026-04-23T15:30:00Z");
    UUID snapshotId = UUID.randomUUID();

    when(popularBookRepository.findBySnapshotIdDescByScore(snapshotId))
        .thenReturn(List.of());

    popularBookAggregationService.rankPopularBooks(PeriodType.WEEKLY, aggregatedAt, snapshotId);

    verify(popularBookRepository).findBySnapshotIdDescByScore(snapshotId);
  }

  @Test
  @DisplayName("같은 랭크를 가진 도서를 조회할 때 타이브레이킹 (성공)")
  void rankPopularBooks_assignsSequentialRankForSameScore() {
    Instant aggregatedAt = Instant.parse("2026-04-21T00:00:00Z");
    UUID snapshotId = UUID.randomUUID();

    PopularBook first = createPopularBook(UUID.randomUUID(), 9.0, snapshotId, aggregatedAt);
    PopularBook second = createPopularBook(UUID.randomUUID(), 9.0, snapshotId, aggregatedAt);
    PopularBook third = createPopularBook(UUID.randomUUID(), 7.0, snapshotId, aggregatedAt);

    when(popularBookRepository.findBySnapshotIdDescByScore(snapshotId))
        .thenReturn(List.of(first, second, third));

    popularBookAggregationService.rankPopularBooks(PeriodType.WEEKLY, aggregatedAt, snapshotId);

    assertEquals(1L, first.getRank());
    assertEquals(2L, second.getRank());
    assertEquals(3L, third.getRank());
    verify(popularBookRepository).findBySnapshotIdDescByScore(snapshotId);
  }

  @Test
  @DisplayName("도서 정보를 바탕으로 인기 도서 객체 생성 테스트 (성공)")
  void toPopularBook_buildsPopularBookFromStat() {
    UUID bookId = UUID.randomUUID();
    UUID snapshotId = UUID.randomUUID();
    Instant aggregatedAt = Instant.parse("2026-04-21T00:00:00Z");
    PopularBookStat stat = new PopularBookStat(bookId, 4L, 4.5);

    PopularBook popularBook = popularBookAggregationService.toPopularBook(
        bookId,
        stat,
        PeriodType.WEEKLY,
        aggregatedAt,
        snapshotId);

    assertEquals(bookId, popularBook.getBookId());
    assertEquals(PeriodType.WEEKLY, popularBook.getPeriodType());
    assertEquals(Instant.parse("2026-04-14T00:00:00Z"), popularBook.getPeriodStart());
    assertEquals(aggregatedAt, popularBook.getPeriodEnd());
    assertEquals(0L, popularBook.getRank());
    assertEquals(4.3d, popularBook.getScore(), 1e-9);
    assertEquals(4L, popularBook.getReviewCount());
    assertEquals(4.5, popularBook.getAvgRating());
    assertEquals(snapshotId, popularBook.getSnapshotId());
  }

  @Test
  @DisplayName("텅 빈 도서 스탯은 0을 반환 (성공)")
  void emptyStat_returnsZeroCounts() {
    UUID bookId = UUID.randomUUID();

    PopularBookStat stat = popularBookAggregationService.emptyStat(bookId);

    assertEquals(bookId, stat.bookId());
    assertEquals(0L, stat.reviewCount());
    assertEquals(0.0, stat.reviewAvgRating());
  }

  private PopularBook createPopularBook(
      UUID bookId,
      double score,
      UUID snapshotId,
      Instant aggregatedAt) {
    return PopularBook.builder()
        .bookId(bookId)
        .periodType(PeriodType.WEEKLY)
        .periodStart(Instant.parse("2026-04-14T00:00:00Z"))
        .periodEnd(aggregatedAt)
        .reviewCount(0L)
        .avgRating(0.0)
        .score(score)
        .rank(0L)
        .snapshotId(snapshotId)
        .build();
  }
}
