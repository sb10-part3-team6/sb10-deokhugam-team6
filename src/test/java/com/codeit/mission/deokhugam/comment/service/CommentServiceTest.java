package com.codeit.mission.deokhugam.comment.service;

import com.codeit.mission.deokhugam.comment.dto.request.CommentCreateRequest;
import com.codeit.mission.deokhugam.comment.dto.request.CommentUpdateRequest;
import com.codeit.mission.deokhugam.comment.dto.response.CommentDto;
import com.codeit.mission.deokhugam.comment.entity.Comment;
import com.codeit.mission.deokhugam.comment.mapper.CommentMapper;
import com.codeit.mission.deokhugam.comment.repository.CommentRepository;
import com.codeit.mission.deokhugam.user.entity.User;
import com.codeit.mission.deokhugam.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private CommentService commentService;

    private UUID userId;
    private UUID reviewId;
    private UUID commentId;
    private String userNickName;
    private Comment comment;
    private User user;
    private CommentDto commentDto;

    @BeforeEach
    void setup() {
        userId = UUID.randomUUID();
        reviewId = UUID.randomUUID();
        commentId = UUID.randomUUID();
        userNickName = "testUser";

        user = mock(User.class);
        when(user.getNickname()).thenReturn(userNickName);

        comment = Comment.builder()
                .reviewId(reviewId)
                .userId(userId)
                .content("test content")
                .build();

        commentDto = mock(CommentDto.class);
    }

    @Test
    @DisplayName("댓글 생성 성공")
    void createCommentSuccess() {
        // given
        CommentCreateRequest request = new CommentCreateRequest(reviewId, userId, "test content");
        given(reviewRepository.existsById(eq(reviewId))).willReturn(true);
        given(userRepository.findById(eq(userId))).willReturn(Optional.of(user));

        Comment savedComment = Comment.builder()
                .reviewId(reviewId)
                .userId(userId)
                .content(request.content())
                .build();

        given(commentMapper.toDto(savedComment, userNickName)).willReturn(commentDto);

        // when
        CommentDto result = commentService.createComment(request);

        // then
        assertThat(result).isEqualTo(commentDto);
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    @DisplayName("댓글 생성 실패 - 리뷰가 존재하지 않음")
    void createCommentFail() {
        // given
        UUID wrongReviewId = UUID.randomUUID();
        CommentCreateRequest request = new CommentCreateRequest(wrongReviewId, userId, "test content");
        given(reviewRepository.existsById(eq(wrongReviewId))).willReturn(false);

        // when

        // then
        assertThatThrownBy(() -> commentService.createComment(request))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("댓글 수정 성공")
    void updateCommentSuccess() {
        // given
        CommentUpdateRequest request = new CommentUpdateRequest("updated content");
        given(commentRepository.findById(eq(commentId))).willReturn(Optional.of(comment));
        given(userRepository.findById(eq(userId))).willReturn(Optional.of(user));
        given(commentMapper.toDto(comment, userNickName)).willReturn(commentDto);

        // when
        CommentDto result = commentService.updateComment(commentId, userId, request);

        // then
        assertThat(result).isEqualTo(commentDto);
    }

    @Test
    @DisplayName("댓글 수정 실패")
    void updateCommentFail() {
        // given
        CommentUpdateRequest request = new CommentUpdateRequest("updated content");
        given(commentRepository.findById(eq(commentId))).willReturn(Optional.empty());

        // when

        // then
        assertThatThrownBy(() -> commentService.updateComment(commentId, userId, request))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("댓글 상세 조회 성공")
    void findCommentSuccess() {
        // given
        given(commentRepository.findById(eq(commentId))).willReturn(Optional.of(comment));
        given(userRepository.findById(eq(userId))).willReturn(Optional.of(user));
        given(commentMapper.toDto(comment, userNickName)).willReturn(commentDto);

        // when
        CommentDto result = commentService.findComment(commentId);

        // then
        assertThat(result).isEqualTo(commentDto);
    }

    @Test
    @DisplayName("댓글 상세 조회 실패")
    void findCommentFail() {
        // given
        given(commentRepository.findById(eq(commentId))).willReturn(Optional.empty());

        // when

        // then
        assertThatThrownBy(() -> commentService.findComment(commentId))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
