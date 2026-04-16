package com.codeit.mission.deokhugam.comment.service;

import com.codeit.mission.deokhugam.comment.dto.request.CommentCreateRequest;
import com.codeit.mission.deokhugam.comment.dto.request.CommentUpdateRequest;
import com.codeit.mission.deokhugam.comment.dto.response.CommentDto;
import com.codeit.mission.deokhugam.comment.entity.Comment;
import com.codeit.mission.deokhugam.comment.mapper.CommentMapper;
import com.codeit.mission.deokhugam.comment.repository.CommentRepository;
import com.codeit.mission.deokhugam.user.entity.User;
import com.codeit.mission.deokhugam.user.repository.UserRepository;
import com.codeit.mission.deokhugam.user.service.UserService;
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
    private final UserRepository userRepository
    private final CommentMapper commentMapper;

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

    public CommentDto updateComment(UUID commentId, UUID reqeustUserId, CommentUpdateRequest request) {
        // 요청자 검증 및 userNickName을 가져오기 위한 user
        User user = userRepository.findById(reqeustUserId).orElseThrow(EntityNotFoundException::new);
        Comment comment = commentRepository.findById(commentId).orElseThrow(EntityNotFoundException::new);
        if (!comment.getUserId().equals(reqeustUserId)) {
            throw new EntityNotFoundException();
        }

        comment.updateContent(request.content());
        Comment updatedComment = commentRepository.save(comment);
        return commentMapper.toDto(updatedComment, user.getNickname());
    }

    public CommentDto find(UUID commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(EntityNotFoundException::new);
        User user = userRepository.findById(comment.getUserId()).orElseThrow(EntityNotFoundException::new);
        return commentMapper.toDto(comment, user.getNickname());
    }

    // 리뷰가 존재하는지 검증
    private void validReviewExists(UUID reviewId) {
        if (!reviewRepository.existById(reviewId)) {
            throw new EntityNotFoundException("리뷰가 존재하지 않습니다.");
        }
    }

    // 유저가 존재하는지 검증
    private void validUserExist(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("유저가 존재하지 않습니다.");
        }
    }

    // 댓글이 존재하는지 검증
    private void validCommentExists(UUID commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new EntityNotFoundException("댓글이 존재하지 않습니다.");
        }
    }
}
