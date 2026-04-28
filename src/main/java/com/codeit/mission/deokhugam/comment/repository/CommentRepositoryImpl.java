package com.codeit.mission.deokhugam.comment.repository;

import com.codeit.mission.deokhugam.comment.dto.request.CommentFindAllRequest;
import com.codeit.mission.deokhugam.comment.entity.Comment;
import com.codeit.mission.deokhugam.comment.entity.CommentStatus;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import static com.codeit.mission.deokhugam.comment.entity.QComment.comment;

@RequiredArgsConstructor
@Repository
public class CommentRepositoryImpl implements CommentRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<Comment> findAllByCursor(CommentFindAllRequest request) {
    String direction = request.direction();
    int limit = request.limit();

    BooleanBuilder builder = new BooleanBuilder();
    builder.and(comment.reviewId.eq(request.reviewId()));
    builder.and(comment.status.eq(CommentStatus.ACTIVE));

    BooleanBuilder cursorCondition = buildCursorCondition(
        direction,
        request.after(),
        parseCursor(request.cursor())
    );

    if (cursorCondition != null) {
      builder.and(cursorCondition);
    }

    return queryFactory
        .selectFrom(comment)
        .where(builder)
        .orderBy(
            createdAtOrder(direction),
            idOrder(direction)
        )
        .limit(limit + 1L)
        .fetch();
  }

  private BooleanBuilder buildCursorCondition(String direction, Instant after, UUID cursor) {
    if (after == null) {
      return null;
    }

    BooleanBuilder builder = new BooleanBuilder();

    if ("ASC".equals(direction)) {
      builder.or(comment.createdAt.gt(after));

      if (cursor != null) {
        builder.or(
            comment.createdAt.eq(after)
                .and(comment.id.gt(cursor))
        );
      }
    } else {
      builder.or(comment.createdAt.lt(after));

      if (cursor != null) {
        builder.or(
            comment.createdAt.eq(after)
                .and(comment.id.lt(cursor))
        );
      }
    }

    return builder;
  }

  private OrderSpecifier<Instant> createdAtOrder(String direction) {
    return new OrderSpecifier<>(
        "ASC".equals(direction) ? Order.ASC : Order.DESC,
        comment.createdAt
    );
  }

  private OrderSpecifier<UUID> idOrder(String direction) {
    return new OrderSpecifier<>(
        "ASC".equals(direction) ? Order.ASC : Order.DESC,
        comment.id
    );
  }

  private UUID parseCursor(String cursor) {
    if (cursor == null || cursor.isBlank()) {
      return null;
    }
    try {
      return UUID.fromString(cursor);
    } catch (IllegalArgumentException e) {
      return null; // 잘못된 cursor는 무시하고 첫 페이지부터 시작
    }
  }
}
