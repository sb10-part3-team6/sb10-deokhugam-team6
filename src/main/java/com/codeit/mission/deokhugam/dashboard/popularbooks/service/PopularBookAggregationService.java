package com.codeit.mission.deokhugam.dashboard.popularbooks.service;

import com.codeit.mission.deokhugam.dashboard.PeriodType;
import com.codeit.mission.deokhugam.dashboard.popularbooks.dto.request.BookReviewAvgRating;
import com.codeit.mission.deokhugam.dashboard.popularbooks.dto.request.BookReviewCount;
import com.codeit.mission.deokhugam.dashboard.popularbooks.dto.request.PopularBookStat;
import com.codeit.mission.deokhugam.dashboard.popularbooks.entity.PopularBook;
import com.codeit.mission.deokhugam.dashboard.popularbooks.repository.PopularBookRepository;
import com.codeit.mission.deokhugam.dashboard.util.Utils;
import com.codeit.mission.deokhugam.review.entity.ReviewStatus;
import com.codeit.mission.deokhugam.review.repository.ReviewRepository;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PopularBookAggregationService {

  // 인기 도서 점수에 필요한 가중치
  private final static double REVIEW_WEIGHT = 0.4;
  private final static double AVERAGE_RATING_WEIGHT = 0.6;

  private final PopularBookRepository popularBookRepository;
  private final ReviewRepository reviewRepository;

  // 한 번 실행하면 도서로부터 인기 도서 집계에 필요한 지수들을 Fetch
  @Transactional(readOnly = true)
  public Map<UUID, PopularBookStat> loadBookStat(PeriodType periodType, Instant aggregatedAt) {
    List<Instant> periods = Utils.calculatePeriod(periodType, aggregatedAt);
    // 집계 기간 범위
    Instant periodStart = periods.get(0);
    Instant periodEnd = periods.get(1);
    log.info("[POPULAR_BOOK_STAT_LOAD_START] periodType={}, periodStart={}, periodEnd={}",
        periodType, periodStart, periodEnd);

    // 책 별 리뷰 개수
    Map<UUID, Long> reviewCountPerBook = new HashMap<>();
    for (BookReviewCount item : reviewRepository.countReviewsPerBook(periodStart, periodEnd,
        ReviewStatus.ACTIVE)) {
      reviewCountPerBook.put(item.bookId(), item.reviewCount());
    }

    // 책 별 리뷰의 평균 점수
    Map<UUID, Double> avgRatingPerBook = new HashMap<>();
    for (BookReviewAvgRating item : reviewRepository.avgRatingsPerBook(periodStart, periodEnd,
        ReviewStatus.ACTIVE)) {
      avgRatingPerBook.put(item.bookId(), item.avgRating());
    }

    // 도서의 ID를 다 가져옴
    Set<UUID> bookIds = new HashSet<>();
    bookIds.addAll(reviewCountPerBook.keySet());
    bookIds.addAll(avgRatingPerBook.keySet());

    // 책 별 지수 (Stat)
    Map<UUID, PopularBookStat> statsByBookId = new HashMap<>();
    for (UUID bookId : bookIds) {
      long reviewCount = reviewCountPerBook.getOrDefault(bookId, 0L);
      double avgRating = avgRatingPerBook.getOrDefault(bookId, 0.0);

      statsByBookId.put(bookId, new PopularBookStat(bookId, reviewCount, avgRating));
    }
    // 책 별 지수를 반환한다.
    log.info("[POPULAR_BOOK_STAT_LOAD_DONE] periodType={}, targetBookCount={}, reviewCountRows={}, avgRatingRows={}",
        periodType, statsByBookId.size(), reviewCountPerBook.size(), avgRatingPerBook.size());
    return statsByBookId;
  }

  @Transactional
  public void rankPopularBooks(PeriodType periodType, Instant aggregatedAt, UUID snapshotId) {
    log.info("[POPULAR_BOOK_RANK_START] periodType={}, aggregatedAt={}, snapshotId={}",
        periodType, aggregatedAt, snapshotId);
    List<PopularBook> popularBooks = popularBookRepository.findBySnapshotIdDescByScore(snapshotId);

    long index = 1L;

    for (PopularBook popularBook : popularBooks) {
      popularBook.updateRank(index);
      index++;
    }
    log.info("[POPULAR_BOOK_RANK_DONE] periodType={}, snapshotId={}, rankedCount={}",
        periodType, snapshotId, popularBooks.size());
  }

  // 정보들을 바탕으로 인기 도서로 가공하는 메서드
  public PopularBook toPopularBook(
      UUID bookId,
      PopularBookStat stat,
      PeriodType periodType,
      Instant aggregatedAt,
      UUID snapshotId) {
    Instant periodStart = periodType.calculateStart(aggregatedAt);
    Instant periodEnd = periodType.calculateEnd(aggregatedAt);

    return PopularBook.builder()
        .bookId(bookId)
        .periodType(periodType)
        .periodStart(periodStart)
        .periodEnd(periodEnd)
        .snapshotId(snapshotId)
        .reviewCount(stat.reviewCount())
        .avgRating(stat.reviewAvgRating())
        .score(calculateScore(stat.reviewCount(), stat.reviewAvgRating()))
        .rank(0L)
        .build();
  }

  public PopularBookStat emptyStat(UUID bookId) {
    return new PopularBookStat(
        bookId,
        0L,
        0.0
    );
  }

  private double calculateScore(long reviewCount, double avgRating) {
    return reviewCount * REVIEW_WEIGHT + avgRating * AVERAGE_RATING_WEIGHT;
  }


}
