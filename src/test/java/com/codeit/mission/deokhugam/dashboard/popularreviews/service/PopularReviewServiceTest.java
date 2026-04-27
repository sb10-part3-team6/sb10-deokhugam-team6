package com.codeit.mission.deokhugam.dashboard.popularreviews.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.codeit.mission.deokhugam.dashboard.DirectionEnum;
import com.codeit.mission.deokhugam.dashboard.DomainType;
import com.codeit.mission.deokhugam.dashboard.PeriodType;
import com.codeit.mission.deokhugam.dashboard.StagingType;
import com.codeit.mission.deokhugam.dashboard.popularreviews.dto.CursorPageResponsePopularReviewDto;
import com.codeit.mission.deokhugam.dashboard.popularreviews.dto.PopularReviewDto;
import com.codeit.mission.deokhugam.dashboard.popularreviews.repository.PopularReviewRepository;
import com.codeit.mission.deokhugam.dashboard.snapshot.AggregateSnapshot;
import com.codeit.mission.deokhugam.dashboard.snapshot.AggregateSnapshotRepository;
import com.codeit.mission.deokhugam.error.DeokhugamException;
import com.codeit.mission.deokhugam.error.ErrorCode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class PopularReviewServiceTest {

  // 임의의 주간 기간의 스냅샷 객체 id
  private static final UUID WEEKLY_SNAPSHOT_ID =
      UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

  @Mock
  private PopularReviewRepository popularReviewRepository;

  @Mock
  private AggregateSnapshotRepository aggregateSnapshotRepository;

  @InjectMocks
  private PopularReviewService popularReviewService;

  @Test
  @DisplayName("인기 리뷰 첫 페이지를 오름차순으로 조회한다")
  void getReviews_firstPageAsc() {
    // given
    // 집계 기간 범위
    LocalDateTime createdAt1 = LocalDateTime.of(2026, 4, 15, 10, 0);
    LocalDateTime createdAt2 = LocalDateTime.of(2026, 4, 15, 10, 1);

    // 리뷰 DTO
    PopularReviewDto review1 =
        popularReviewDto("review-1", "book-1", "user-1", PeriodType.WEEKLY, createdAt1, 1L, 33.0);
    PopularReviewDto review2 =
        popularReviewDto("review-2", "book-2", "user-2", PeriodType.WEEKLY, createdAt2, 2L, 24.0);

    // Weekly 스냅샷 객체 생성하도록 설정
    when(aggregateSnapshotRepository.findTopByDomainTypeAndPeriodTypeAndStagingTypeOrderByCreatedAtDesc(
        DomainType.POPULAR_REVIEW, PeriodType.WEEKLY, StagingType.PUBLISHED))
        .thenReturn(Optional.of(publishedSnapshot(PeriodType.WEEKLY, WEEKLY_SNAPSHOT_ID)));

    // Weekly 스냅샷 객체를 바탕으로 인기 리뷰를 스코어 기준으로 오름차순으로 뽑아옴. (review1, review2)
    when(popularReviewRepository.findRankingDtosBySnapshotIdAsc(
        WEEKLY_SNAPSHOT_ID, null, null, PageRequest.of(0, 51)))
        .thenReturn(List.of(review1, review2));

    // 해당 스냅샷의 인기 리뷰가 몇개 있는지 (2개) 반환하도록 설정
    when(popularReviewRepository.countRankingsBySnapshotId(WEEKLY_SNAPSHOT_ID)).thenReturn(2L);

    // when
    CursorPageResponsePopularReviewDto result =
        popularReviewService.getReviews(PeriodType.WEEKLY, DirectionEnum.ASC, null, null, 50);

    // then
    assertEquals(List.of(review1, review2), result.content()); // 결과가 review1, review2 순으로 나왔는가?
    assertEquals(50, result.size()); // 사이즈가 50인가?
    assertEquals(2L, result.totalElements()); // 결과의 요소 개수가 2개인가?
    assertFalse(result.hasNext()); // 첫 페이지이므로 hasNext 는 false
    assertNull(result.nextCursor()); // 커서도 null
    assertNull(result.nextAfter());

    // 스냅샷 레포지토리에서 해당 메서드가 한번 실행되었는가
    verify(aggregateSnapshotRepository)
        .findTopByDomainTypeAndPeriodTypeAndStagingTypeOrderByCreatedAtDesc(
            DomainType.POPULAR_REVIEW, PeriodType.WEEKLY, StagingType.PUBLISHED);

    verify(popularReviewRepository)
        .findRankingDtosBySnapshotIdAsc(WEEKLY_SNAPSHOT_ID, null, null, PageRequest.of(0, 51));
    verify(popularReviewRepository).countRankingsBySnapshotId(WEEKLY_SNAPSHOT_ID);
  }

  @Test
  @DisplayName("인기 리뷰 첫 페이지를 내림차순으로 조회한다")
  void getReviews_firstPageDesc() {
    // given
    LocalDateTime createdAt1 = LocalDateTime.of(2026, 4, 15, 10, 0);
    LocalDateTime createdAt2 = LocalDateTime.of(2026, 4, 15, 10, 1);

    // 인기 리뷰 Dto
    PopularReviewDto review1 =
        popularReviewDto("review-1", "book-1", "user-1", PeriodType.WEEKLY, createdAt1, 1L, 33.0);
    PopularReviewDto review2 =
        popularReviewDto("review-2", "book-2", "user-2", PeriodType.WEEKLY, createdAt2, 2L, 24.0);

    // WEEKLY 스냅샷 생성
    when(aggregateSnapshotRepository.findTopByDomainTypeAndPeriodTypeAndStagingTypeOrderByCreatedAtDesc(
        DomainType.POPULAR_REVIEW, PeriodType.WEEKLY, StagingType.PUBLISHED))
        .thenReturn(Optional.of(publishedSnapshot(PeriodType.WEEKLY, WEEKLY_SNAPSHOT_ID)));

    // 주간 기간동안 인기 리뷰를 조회 (첫페이지) 내림차순으로 조회하면 review2, review1 순으로 반환
    when(popularReviewRepository.findRankingDtosBySnapshotIdDesc(
        WEEKLY_SNAPSHOT_ID, null, null, PageRequest.of(0, 51)))
        .thenReturn(List.of(review2, review1));
    // 해당 스냅샷을 참조하는 인기 리뷰는 두개
    when(popularReviewRepository.countRankingsBySnapshotId(WEEKLY_SNAPSHOT_ID)).thenReturn(2L);

    // when
    CursorPageResponsePopularReviewDto result =
        popularReviewService.getReviews(PeriodType.WEEKLY, DirectionEnum.DESC, null, null, 50);

    // then
    assertEquals(List.of(review2, review1), result.content());
    assertEquals(50, result.size());
    assertEquals(2L, result.totalElements());
    assertFalse(result.hasNext());
    assertNull(result.nextCursor());
    assertNull(result.nextAfter());

    verify(aggregateSnapshotRepository)
        .findTopByDomainTypeAndPeriodTypeAndStagingTypeOrderByCreatedAtDesc(
            DomainType.POPULAR_REVIEW, PeriodType.WEEKLY, StagingType.PUBLISHED);
    verify(popularReviewRepository)
        .findRankingDtosBySnapshotIdDesc(WEEKLY_SNAPSHOT_ID, null, null, PageRequest.of(0, 51));
    verify(popularReviewRepository).countRankingsBySnapshotId(WEEKLY_SNAPSHOT_ID);
  }

  @Test
  @DisplayName("다음 페이지가 있으면 next cursor를 반환한다")
  void getReviews_returnsNextCursor() {
    // given
    LocalDateTime createdAt1 = LocalDateTime.of(2026, 4, 15, 10, 0);
    LocalDateTime createdAt2 = LocalDateTime.of(2026, 4, 15, 10, 1);
    LocalDateTime createdAt3 = LocalDateTime.of(2026, 4, 15, 10, 2);

    // 조회하고자 하는 인기 리뷰는 3개
    PopularReviewDto review1 =
        popularReviewDto("review-1", "book-1", "user-1", PeriodType.WEEKLY, createdAt1, 1L, 10.0);
    PopularReviewDto review2 =
        popularReviewDto("review-2", "book-2", "user-2", PeriodType.WEEKLY, createdAt2, 2L, 9.0);
    PopularReviewDto review3 =
        popularReviewDto("review-3", "book-3", "user-3", PeriodType.WEEKLY, createdAt3, 3L, 8.0);

    // 도메인 종류, 기간 종류, 스냅샷 스테이징 상태를 기반으로 스냅샷 객체들을 뽑아냄
    when(aggregateSnapshotRepository.findTopByDomainTypeAndPeriodTypeAndStagingTypeOrderByCreatedAtDesc(
        DomainType.POPULAR_REVIEW, PeriodType.WEEKLY, StagingType.PUBLISHED))
        .thenReturn(Optional.of(publishedSnapshot(PeriodType.WEEKLY, WEEKLY_SNAPSHOT_ID)));

    // 오름차 순으로 인기 리뷰 리스트 조회 페이지 사이즈는 (3)
    when(popularReviewRepository.findRankingDtosBySnapshotIdAsc(
        WEEKLY_SNAPSHOT_ID, null, null, PageRequest.of(0, 3)))
        .thenReturn(List.of(review1, review2, review3));

    // 주간 스냅샷을 참조하고 있는 인기 리뷰의 개수는 3개
    when(popularReviewRepository.countRankingsBySnapshotId(WEEKLY_SNAPSHOT_ID)).thenReturn(3L);

    // when
    // 토탈 페이지 사이즈 3에서 2 만큼 조회 -> hasNext = true
    CursorPageResponsePopularReviewDto result =
        popularReviewService.getReviews(PeriodType.WEEKLY, DirectionEnum.ASC, null, null, 2);

    // then
    assertTrue(result.hasNext()); // 다음 페이지가 존재하는지 검증
    assertEquals(List.of(review1, review2), result.content()); // 첫 페이지의 사이즈는 두개 -> review1, review2 두개
    assertEquals("2", result.nextCursor());
    assertEquals(createdAt2.toString(), result.nextAfter());
    assertEquals(2, result.size());
    assertEquals(3L, result.totalElements());
  }

  @Test
  @DisplayName("cursor 형식이 올바르지 않으면 예외가 발생한다")
  void getReviews_invalidCursor() {
    // given + when + then
    // 우리가 예상하는 예외
    DeokhugamException exception =
        assertThrows(
            DeokhugamException.class,
            () ->
                popularReviewService.getReviews(
                    PeriodType.WEEKLY,
                    DirectionEnum.DESC,
                    "invalid-cursor",
                    "2026-04-15T10:00:00",
                    50));
    // then
    assertEquals(ErrorCode.CURSOR_OR_AFTER_FORMAT_NOT_VALID, exception.getErrorCode());
    verifyNoInteractions(popularReviewRepository, aggregateSnapshotRepository);
  }

  @Test
  @DisplayName("after 형식이 올바르지 않으면 예외가 발생한다")
  void getReviews_invalidAfter() {
    // when + then
    DeokhugamException exception =
        assertThrows(
            DeokhugamException.class,
            () ->
                popularReviewService.getReviews(
                    PeriodType.WEEKLY, DirectionEnum.DESC, "3", "not-a-date", 50));

    // 에러코드 검증
    assertEquals(ErrorCode.CURSOR_OR_AFTER_FORMAT_NOT_VALID, exception.getErrorCode());

    verifyNoInteractions(popularReviewRepository, aggregateSnapshotRepository);
  }

  @Test
  @DisplayName("cursor와 after는 함께 제공되어야 한다")
  void getReviews_cursorAfterMismatch() {
    // when + then
    DeokhugamException exception =
        assertThrows(
            DeokhugamException.class,
            () ->
                popularReviewService.getReviews(
                    PeriodType.WEEKLY, DirectionEnum.DESC, "3", null, 50));

    assertEquals(ErrorCode.CURSOR_AFTER_NOT_PROVIDED_TOGETHER, exception.getErrorCode());
    verifyNoInteractions(popularReviewRepository, aggregateSnapshotRepository);
  }

  @Test
  @DisplayName("발행된 스냅샷이 없으면 빈 Content를 반환한다.")
  void getReviews_snapshotNotFound() {
    // when
    when(aggregateSnapshotRepository.findTopByDomainTypeAndPeriodTypeAndStagingTypeOrderByCreatedAtDesc(
        DomainType.POPULAR_REVIEW, PeriodType.MONTHLY, StagingType.PUBLISHED))
        .thenReturn(Optional.empty());

    // when
    CursorPageResponsePopularReviewDto result = popularReviewService.getReviews(PeriodType.MONTHLY, DirectionEnum.DESC, null, null, 1);

    assertEquals(0, result.content().size()); // content가 비어있는지 확인
    assertTrue(result.content().isEmpty()); // content는 비어있음.
    assertEquals(0L, result.totalElements()); // 총 요소의 개수도 0
    assertFalse(result.hasNext()); // next도 없으며
    assertNull(result.nextCursor()); // 커서도 없다.
    assertNull(result.nextAfter());

    verify(aggregateSnapshotRepository) // 도메인 타입과 period, 스냅샷 스테이징 상태를 통한 조회 쿼리 메서드를 실행했는가?
        .findTopByDomainTypeAndPeriodTypeAndStagingTypeOrderByCreatedAtDesc(
            DomainType.POPULAR_REVIEW, PeriodType.MONTHLY, StagingType.PUBLISHED);
    verifyNoInteractions(popularReviewRepository);
  }

  // 기간과 ID를 받아 스냅샷 객체를 생성하는 메서드
  private AggregateSnapshot publishedSnapshot(PeriodType periodType, UUID snapshotId) {
    return AggregateSnapshot.builder()
        .domainType(DomainType.POPULAR_REVIEW)
        .snapshotId(snapshotId)
        .periodType(periodType)
        .aggregatedAt(LocalDateTime.of(2026, 4, 15, 0, 0))
        .stagingType(StagingType.PUBLISHED)
        .build();
  }

  private PopularReviewDto popularReviewDto(
      String reviewContent,
      String bookTitle,
      String userNickname,
      PeriodType periodType,
      LocalDateTime createdAt,
      long rank,
      double score) {
    return new PopularReviewDto(
        UUID.randomUUID(),
        UUID.randomUUID(),
        UUID.randomUUID(),
        bookTitle,
        "thumbnail",
        UUID.randomUUID(),
        userNickname,
        reviewContent,
        4.5,
        periodType,
        createdAt,
        rank,
        score,
        3L,
        2L);
  }
}
