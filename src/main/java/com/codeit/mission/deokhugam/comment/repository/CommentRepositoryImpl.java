package com.codeit.mission.deokhugam.comment.repository;

import com.codeit.mission.deokhugam.comment.dto.request.CommentFindAllRequest;
import com.codeit.mission.deokhugam.comment.entity.Comment;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.codeit.mission.deokhugam.comment.entity.QComment.comment;

@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepositoryCustom{
    private final JPAQueryFactory queryFactory;

    @Override
    public List<Comment> filter(CommentFindAllRequest request) {
        BooleanBuilder builder = new BooleanBuilder();

        if (request.reviewId() != null) {
            builder.and(comment.reviewId.eq(request.reviewId()));
        }

        boolean isAsc = "asc".equalsIgnoreCase(request.direction());

        // 커서 조건
        if (request.cursor() != null) {

        }
        return List.of();
    }

    @Override
    public int count(CommentFindAllRequest request) {
        BooleanBuilder builder = new BooleanBuilder();

        if (request.reviewId() != null) {

        }
        return 0;
    }

    @Override
    public List<Comment> exports(CommentFindAllRequest request) {
        return List.of();
    }
}
