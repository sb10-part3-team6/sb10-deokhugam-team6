package com.codeit.mission.deokhugam.comment.service;

import com.codeit.mission.deokhugam.comment.dto.request.CommentCreateRequest;
import com.codeit.mission.deokhugam.comment.dto.request.CommentFindAllRequest;
import com.codeit.mission.deokhugam.comment.dto.request.CommentUpdateRequest;
import com.codeit.mission.deokhugam.comment.dto.response.CommentDto;
import com.codeit.mission.deokhugam.comment.dto.response.CursorPageResponseCommentDto;
import com.codeit.mission.deokhugam.comment.entity.Comment;
import com.codeit.mission.deokhugam.comment.exception.CommentAuthorException;
import com.codeit.mission.deokhugam.comment.mapper.CommentMapper;
import com.codeit.mission.deokhugam.comment.repository.CommentRepository;
import com.codeit.mission.deokhugam.review.entity.Review;
import com.codeit.mission.deokhugam.review.entity.ReviewStatus;
import com.codeit.mission.deokhugam.review.exception.ReviewNotFoundException;
import com.codeit.mission.deokhugam.review.repository.ReviewRepository;
import com.codeit.mission.deokhugam.user.entity.User;
import com.codeit.mission.deokhugam.user.entity.UserStatus;
import com.codeit.mission.deokhugam.user.exception.UserNotFoundException;
import com.codeit.mission.deokhugam.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

  private final CommentRepository commentRepository;
  private final ReviewRepository reviewRepository;
  private final UserRepository userRepository;
  private final CommentMapper commentMapper;

  // 댓글 등록
  @Transactional
  public CommentDto createComment(CommentCreateRequest request) {
    // Review 검증
    Review review = validReviewExists(request.reviewId());
    // User검증 + userNickName을 가져오기 위한 user
    User user = validUserExists(request.userId());

    Comment comment = Comment.builder()
        .reviewId(request.reviewId())
        .userId(request.userId())
        .content(request.content())
        .build();

    Comment savedComment = commentRepository.saveAndFlush(comment);
    reviewRepository.incrementCommentCount(request.reviewId());

    return commentMapper.toDto(savedComment, user.getNickname());
  }

  // 댓글 수정
  @Transactional
  public CommentDto updateComment(UUID commentId, UUID requestUserId,
      CommentUpdateRequest request) {
    // 요청자 검증 및 userNickName을 가져오기 위한 user
    User user = validUserExists(requestUserId);
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(EntityNotFoundException::new);
    if (!comment.getUserId().equals(requestUserId)) {
      throw new CommentAuthorException();
    }

    comment.updateContent(request.content());
    Comment updatedComment = commentRepository.save(comment);
    return commentMapper.toDto(updatedComment, user.getNickname());
  }

  // 댓글 조회
  public CommentDto findComment(UUID commentId) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(EntityNotFoundException::new);
    User user = userRepository.findById(comment.getUserId())
        .orElseThrow(EntityNotFoundException::new);
    return commentMapper.toDto(comment, user.getNickname());
  }

  // 댓글 목록 조회
  public CursorPageResponseCommentDto findAllComments(CommentFindAllRequest request) {
    validReviewExists(request.reviewId());
    int limit = request.limit();
    List<Comment> comments = commentRepository.findAllByCursor(request);

    boolean hasNext = comments.size() > limit;
    if (hasNext) {
      comments = comments.subList(0, limit);
    }

    Map<UUID, String> nicknameMap = userRepository.findAllById(
            comments.stream()
                .map(Comment::getUserId)
                .distinct()
                .toList()
        ).stream()
        .collect(java.util.stream.Collectors.toMap(
            User::getId,
            User::getNickname
        ));

    List<CommentDto> content = comments.stream()
        .map(comment -> commentMapper.toDto(
            comment,
            nicknameMap.get(comment.getUserId())
        ))
        .toList();

    String nextCursor = null;
    LocalDateTime nextAfter = null;

    if (hasNext && !comments.isEmpty()) {
      Comment last = comments.get(comments.size() - 1);
      nextCursor = last.getId().toString();
      nextAfter = last.getCreatedAt();
    }

    int totalElements = commentRepository.countByReviewId(request.reviewId());

    return new CursorPageResponseCommentDto(
        content,
        nextCursor,
        nextAfter,
        content.size(),
        totalElements,
        hasNext
    );
  }

  // 리뷰가 존재하는지 검증
  private Review validReviewExists(UUID reviewId) {
    Review review = reviewRepository.findById(reviewId).orElseThrow(EntityNotFoundException::new);

    // 리뷰 상태가 Delete면 조회 실패
    if (review.getStatus() == ReviewStatus.DELETED) {
      throw new ReviewNotFoundException(reviewId);
    }

    return review;
  }

  // 유저가 존재하는지 검증
  private User validUserExists(UUID userId) {
    User user = userRepository.findById(userId).orElseThrow(EntityNotFoundException::new);

    // 유저 상태가 Delete면 조회 실패
    if (user.getStatus() == UserStatus.DELETED) {
      throw new UserNotFoundException(userId);
    }

    return user;
  }
}
