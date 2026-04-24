package com.codeit.mission.deokhugam.dashboard.popularbooks.service;

import com.codeit.mission.deokhugam.dashboard.PeriodType;
import com.codeit.mission.deokhugam.dashboard.popularbooks.dto.BookReviewAvgRating;
import com.codeit.mission.deokhugam.dashboard.popularbooks.dto.BookReviewCount;
import com.codeit.mission.deokhugam.dashboard.popularbooks.dto.PopularBookStat;
import com.codeit.mission.deokhugam.dashboard.popularbooks.repository.PopularBookRepository;
import com.codeit.mission.deokhugam.dashboard.util.Utils;
import com.codeit.mission.deokhugam.review.entity.ReviewStatus;
import com.codeit.mission.deokhugam.review.repository.ReviewRepository;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PopularBookAggregationService {
  private PopularBookRepository popularBookRepository;
  private ReviewRepository reviewRepository;

  public Map<UUID, PopularBookStat> loadBookStat(PeriodType periodType, LocalDateTime aggregatedAt){
    List<LocalDateTime> periods = Utils.calculatePeriod(periodType, aggregatedAt);
    LocalDateTime periodStart = periods.get(0);
    LocalDateTime periodEnd = periods.get(1);

    Map<UUID, Long> reviewCountPerBook = new HashMap<>();
    for(BookReviewCount item : reviewRepository.countReviewsPerBook(periodStart, periodEnd, ReviewStatus.ACTIVE)){
      reviewCountPerBook.put(item.bookId(), item.reviewCount());
    }

    Map<UUID, Double> avgRatingPerBook = new HashMap<>();
    for (BookReviewAvgRating item : reviewRepository.avgRatingsPerBook(periodStart, periodEnd, ReviewStatus.ACTIVE)) {
      avgRatingPerBook.put(item.bookId(), item.avgRating());
    }

    // 도서의 ID를 다 가져옴
    Set<UUID> bookIds = new HashSet<>();
    bookIds.addAll(reviewCountPerBook.keySet());
    bookIds.addAll(avgRatingPerBook.keySet());

    Map<UUID, PopularBookStat> statsByBookId = new HashMap<>();
    for(UUID bookId : bookIds){
      long reviewCount = reviewCountPerBook.getOrDefault(bookId, 0L);
      double avgRating = avgRatingPerBook.getOrDefault(bookId, 0.0);

      statsByBookId.put(bookId, new PopularBookStat(bookId, reviewCount, avgRating));
    }
    return statsByBookId;
  }


}
