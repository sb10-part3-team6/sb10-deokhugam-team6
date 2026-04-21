package com.codeit.mission.deokhugam.comment.repository;

import com.codeit.mission.deokhugam.comment.dto.request.CommentFindAllRequest;
import com.codeit.mission.deokhugam.comment.entity.Comment;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.codeit.mission.deokhugam.comment.entity.QComment.comment;

@RequiredArgsConstructor
@Repository
public class CommentRepositoryImpl implements CommentRepositoryCustom{
    private final JPAQueryFactory queryFactory;

    @Override
    public List<Comment> findAllByCursor(CommentFindAllRequest request) {
        String direction = normalizeDirection(request.direction());
        int limit = normalizeLimit(request.limit());

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(comment.reviewId.eq(request.reviewId()));

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

    private BooleanBuilder buildCursorCondition(String direction, LocalDateTime after, UUID cursor) {
        if (after == null) {
            return null;
        }

        BooleanBuilder builder = new BooleanBuilder();

        if ("asc".equals(direction)) {
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
    private OrderSpecifier<LocalDateTime> createdAtOrder(String direction) {
        return new OrderSpecifier<>(
                "asc".equals(direction) ? Order.ASC : Order.DESC,
                comment.createdAt
        );
    }

    private OrderSpecifier<UUID> idOrder(String direction) {
        return new OrderSpecifier<>(
                "asc".equals(direction) ? Order.ASC : Order.DESC,
                comment.id
        );
    }

    private String normalizeDirection(String direction) {
        return "asc".equalsIgnoreCase(direction) ? "asc" : "desc";
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return 50;
        }
        return Math.min(limit, 100);
    }

    private UUID parseCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        return UUID.fromString(cursor);
    }
}
