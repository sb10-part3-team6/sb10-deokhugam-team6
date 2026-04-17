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
import com.codeit.mission.deokhugam.user.entity.User;
import com.codeit.mission.deokhugam.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

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
        validReviewExists(request.reviewId());
        // User검증 + userNickName을 가져오기 위한 user
        User user = userRepository.findById(request.userId()).orElseThrow(EntityNotFoundException::new);

        Comment comment = Comment.builder()
                .reviewId(request.reviewId())
                .userId(request.userId())
                .content(request.content())
                .build();

        Comment savedComment = commentRepository.save(comment);
        return commentMapper.toDto(savedComment, user.getNickname());
    }

    // 댓글 수정
    @Transactional
    public CommentDto updateComment(UUID commentId, UUID requestUserId, CommentUpdateRequest request) {
        // 요청자 검증 및 userNickName을 가져오기 위한 user
        User user = userRepository.findById(requestUserId).orElseThrow(EntityNotFoundException::new);
        Comment comment = commentRepository.findById(commentId).orElseThrow(EntityNotFoundException::new);
        if (!comment.getUserId().equals(requestUserId)) {
            throw new CommentAuthorException();
        }

        comment.updateContent(request.content());
        Comment updatedComment = commentRepository.save(comment);
        return commentMapper.toDto(updatedComment, user.getNickname());
    }

    // 댓글 조회
    public CommentDto findComment(UUID commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(EntityNotFoundException::new);
        User user = userRepository.findById(comment.getUserId()).orElseThrow(EntityNotFoundException::new);
        return commentMapper.toDto(comment, user.getNickname());
    }

    // 댓글 목록 조회
    public CursorPageResponseCommentDto<CommentDto> findComments(CommentFindAllRequest request) {
        return null;
    }

    // 리뷰가 존재하는지 검증
    private void validReviewExists(UUID reviewId) {
        if (!reviewRepository.existsById(reviewId)) {
            throw new EntityNotFoundException("리뷰가 존재하지 않습니다.");
        }
    }
}
