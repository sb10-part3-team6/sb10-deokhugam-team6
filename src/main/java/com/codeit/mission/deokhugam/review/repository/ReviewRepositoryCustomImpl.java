package com.codeit.mission.deokhugam.review.repository;

import com.codeit.mission.deokhugam.review.dto.request.ReviewSearchConditionDto;
import com.codeit.mission.deokhugam.review.entity.Review;
import com.codeit.mission.deokhugam.review.entity.ReviewStatus;
import com.codeit.mission.deokhugam.review.exception.InvalidCursorFormatException;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

import static com.codeit.mission.deokhugam.review.entity.QReview.review;

@RequiredArgsConstructor
public class ReviewRepositoryCustomImpl implements ReviewRepositoryCustom {

  private final JPAQueryFactory jpaQueryFactory;

  // 필터링 + 내림차순 정렬 + 커서 기반 페이지네이션이 적용된 연동 목록 조회
  @Override
  public List<Review> searchReviews(ReviewSearchConditionDto condition) {
    // 1. 동적 쿼리 내 WHERE 절에 추가될 빌더 객체 생성
    BooleanBuilder filterBuilder = builderFilterCondition(condition);

    // 2. 사용자가 요청한 정렬 조건 (orderBy): 기본값이 생성 시점이므로 평점만 비교
    boolean isRatingOrder = "rating".equals(condition.orderBy());

    // 3. 커서 기반 페이지네이션: 커서와 보조 커서 (after)가 모두 있을 때만 다음 페이지 조회
    if (condition.cursor() != null && condition.after() != null) {
      // 페이지네이션 조건을 담을 빌더 객체 생성
      BooleanBuilder cursorBuilder = new BooleanBuilder();

      // 보조 커서 (after) 이전 페이지의 마지막 요소 생성 시점
      LocalDateTime after = condition.after();

      // 정렬 기준 필드 = 평점(rating)
      if (isRatingOrder) {
        // 커서 (cursor): 이전 페이지의 마지막 요소 평점
        int cursorRating;

        try {
          cursorRating = Integer.parseInt(condition.cursor());
        } catch (NumberFormatException e) {
          // 숫자가 아닌 값이 평점 값으로 들어온 경우
          throw new InvalidCursorFormatException();
        }

        // 마지막 요소의 평점보다 낮은 요소와 평점은 같지만 생성 시간이 오래된 요소
        cursorBuilder.or(review.rating.lt(cursorRating));
        cursorBuilder.or(review.rating.eq(cursorRating).and(review.createdAt.lt(after)));
      }
      // 기본 정렬 기준 필드 = 생성 시간(createdAt)
      else {
        // 커서 (cursor): 이전 페이지의 마지막 요소 ID
        UUID cursorId;

        try {
          cursorId = UUID.fromString(condition.cursor());
        } catch (IllegalArgumentException e) {
          // UUID 형식이 아닌 값이 ID 값으로 들어온 경우
          throw new InvalidCursorFormatException();
        }

        // 마지막 요소의 생성 시간보다 오래된 요소와 생성 시간은 같지만 ID 값이 작은 요소
        cursorBuilder.or(review.createdAt.lt(after));
        cursorBuilder.or(review.createdAt.eq(after).and(review.id.lt(cursorId)));
      }
      // 페이지네이션 빌더 객체를 WHERE 빌더 객체에 AND로 결합
      filterBuilder.and(cursorBuilder);
    }

    // 4. 정렬 조건 설정
    OrderSpecifier<?>[] orderSpecifiers = isRatingOrder
        // 평점 정렬: 평점 -> 생성 시간 -> ID 내림차순
        ? new OrderSpecifier<?>[]{review.rating.desc(), review.createdAt.desc(), review.id.desc()}
        // 시간 정렬: 생성 시간 -> ID
        : new OrderSpecifier<?>[]{review.createdAt.desc(), review.id.desc()};

    // 6. 동적 쿼리 실행
    return jpaQueryFactory
        .selectFrom(review)
        // 성능 최적화: N + 1 문제 방지
        .leftJoin(review.book).fetchJoin()
        .leftJoin(review.user).fetchJoin()
        .where(filterBuilder)
        // 평점 -> 생성 시간 -> id 내림차순 적용
        .orderBy(orderSpecifiers)
        .limit(condition.limit() + 1)
        .fetch();
  }

  // 필터링 조건이 적용된 연동 목록의 전체 개수 조회
  @Override
  public long countWithFilter(ReviewSearchConditionDto condition) {
    // 1. 동적 쿼리 내 WHERE 절에 추가될 빌더 객체 생성
    BooleanBuilder filterBuilder = builderFilterCondition(condition);

    // 2. 동적 쿼리 실행
    Long totalCount = jpaQueryFactory
        .select(review.count())
        .from(review)
        .where(filterBuilder)
        .fetchOne();

    return totalCount != null ? totalCount : 0L;
  }

  // 동적 쿼리 필터링
  private BooleanBuilder builderFilterCondition(ReviewSearchConditionDto condition) {
    // 1. 동적 쿼리 내 WHERE 절에 추가될 빌더 객체 생성
    BooleanBuilder filterBuilder = new BooleanBuilder();

    // 2. 기본 상태 필터: 활성(ACTIVE)인 리뷰만 조회
    filterBuilder.and(review.status.eq(ReviewStatus.ACTIVE));

    // 3. [완전 일치 조건]: 특정 도서 ID 필터
    if (condition.bookId() != null) {
      filterBuilder.and(review.book.id.eq(condition.bookId()));
    }

    // 4. [완전 일치 조건]: 특정 사용자 ID 필터
    if (condition.userId() != null) {
      filterBuilder.and(review.user.id.eq(condition.userId()));
    }

    // 5. [부분 일치 조건]: 키워드 필터
    if (condition.keyword() != null && !condition.keyword().isBlank()) {
      // 키워드 검색을 위한 빌더 객체 생성
      BooleanBuilder keywordBuilder = new BooleanBuilder();

      // 리뷰 내용, 작성자 닉네임, 도서 제목 중 하나라도 부분 일치하면 검색
      keywordBuilder.or(review.content.contains(condition.keyword()));
      keywordBuilder.or(review.user.nickname.contains(condition.keyword()));
      keywordBuilder.or(review.book.title.contains(condition.keyword()));

      // 키워드 빌더 객체를 WHERE 빌더 객체에 AND도 결합
      filterBuilder.and(keywordBuilder);
    }

    return filterBuilder;
  }
}