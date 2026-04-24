package com.codeit.mission.deokhugam.book.repository;

import com.codeit.mission.deokhugam.book.entity.BookStatus;
import com.codeit.mission.deokhugam.book.entity.SortDirection;
import com.codeit.mission.deokhugam.book.entity.Book;
import com.codeit.mission.deokhugam.book.entity.QBook;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class BookRepositoryImpl implements BookRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Book> findAllByCursor(
            String keyword,
            String orderBy,
            SortDirection direction,
            Object cursor,
            LocalDateTime after,
            int limit
    ) {
        QBook book = QBook.book;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(book.bookStatus.ne(BookStatus.DELETED));

        // 1. keyword 조건
        if (keyword != null && !keyword.isBlank()) {
            builder.and(
                    book.title.containsIgnoreCase(keyword)
                            .or(book.author.containsIgnoreCase(keyword))
                            .or(book.isbn.containsIgnoreCase(keyword))
            );
        }

        // 2. 커서 조건
        BooleanBuilder cursorCondition = buildCursorCondition(
                book,
                orderBy,
                direction,
                cursor,
                after
        );

        if (cursorCondition != null) {
            builder.and(cursorCondition);
        }

        return queryFactory
                .selectFrom(book)
                .where(builder)
                .orderBy(
                        getOrder(orderBy, direction),
                        createdAtOrder(direction)
                )
                .limit(limit + 1)
                .fetch();
    }

    private BooleanBuilder buildCursorCondition(
            QBook book,
            String orderBy,
            SortDirection direction,
            Object cursor,
            LocalDateTime after
    ) {
        if (cursor == null || after == null) return null;

        BooleanBuilder builder = new BooleanBuilder();

        switch (orderBy) {

            case "title" -> {
                String value = (String) cursor;

                if (direction == SortDirection.ASC) {
                    builder.or(book.title.gt(value));
                    builder.or(book.title.eq(value).and(book.createdAt.gt(after)));
                } else {
                    builder.or(book.title.lt(value));
                    builder.or(book.title.eq(value).and(book.createdAt.lt(after)));
                }
            }

            case "publishedDate" -> {
                LocalDate value = (LocalDate) cursor;

                if (direction == SortDirection.ASC) {
                    builder.or(book.publishedDate.gt(value));
                    builder.or(book.publishedDate.eq(value).and(book.createdAt.gt(after)));
                } else {
                    builder.or(book.publishedDate.lt(value));
                    builder.or(book.publishedDate.eq(value).and(book.createdAt.lt(after)));
                }
            }

            case "rating" -> {
                Double value = (Double) cursor;

                if (direction == SortDirection.ASC) {
                    builder.or(book.rating.gt(value));
                    builder.or(book.rating.eq(value).and(book.createdAt.gt(after)));
                } else {
                    builder.or(book.rating.lt(value));
                    builder.or(book.rating.eq(value).and(book.createdAt.lt(after)));
                }
            }

            case "reviewCount" -> {
                Integer value = (Integer) cursor;

                if (direction == SortDirection.ASC) {
                    builder.or(book.reviewCount.gt(value));
                    builder.or(book.reviewCount.eq(value).and(book.createdAt.gt(after)));
                } else {
                    builder.or(book.reviewCount.lt(value));
                    builder.or(book.reviewCount.eq(value).and(book.createdAt.lt(after)));
                }
            }

            default -> throw new IllegalArgumentException("Invalid orderBy");
        }

        return builder;
    }

    private OrderSpecifier<?> getOrder(String orderBy, SortDirection direction) {
        Order order = direction == SortDirection.ASC ? Order.ASC : Order.DESC;
        return new OrderSpecifier(order, getField(QBook.book, orderBy));
    }

    private OrderSpecifier<LocalDateTime> createdAtOrder(SortDirection direction) {
        return new OrderSpecifier<>(
                direction == SortDirection.ASC ? Order.ASC : Order.DESC,
                QBook.book.createdAt
        );
    }

    private ComparableExpressionBase<? extends Comparable> getField(QBook book, String orderBy) {
        return switch (orderBy) {
            case "title" -> book.title;
            case "publishedDate" -> book.publishedDate;
            case "rating" -> book.rating;
            case "reviewCount" -> book.reviewCount;
            default -> throw new IllegalArgumentException("Invalid orderBy");
        };
    }

    @Override
    public long countByCondition(String keyword) {
        QBook book = QBook.book;

        BooleanBuilder builder = new BooleanBuilder();

        // 삭제 제외
        builder.and(book.bookStatus.ne(BookStatus.DELETED));

        // 검색 조건
        if (keyword != null && !keyword.isBlank()) {
            builder.and(
                    book.title.containsIgnoreCase(keyword)
                            .or(book.author.containsIgnoreCase(keyword))
                            .or(book.isbn.containsIgnoreCase(keyword))
            );
        }

        return queryFactory
                .select(book.count())
                .from(book)
                .where(builder)
                .fetchOne();
    }
}
