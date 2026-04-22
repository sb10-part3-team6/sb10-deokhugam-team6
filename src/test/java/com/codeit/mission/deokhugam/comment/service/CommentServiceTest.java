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
import com.codeit.mission.deokhugam.review.repository.ReviewRepository;
import com.codeit.mission.deokhugam.user.entity.User;
import com.codeit.mission.deokhugam.user.entity.UserStatus;
import com.codeit.mission.deokhugam.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
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
    private CommentFindAllRequest findAllRequest;

    @BeforeEach
    void setup() {
        userId = UUID.randomUUID();
        reviewId = UUID.randomUUID();
        commentId = UUID.randomUUID();
        userNickName = "testUser";

        user = mock(User.class);
        when(user.getId()).thenReturn(userId);
        when(user.getNickname()).thenReturn(userNickName);
        when(user.getStatus()).thenReturn(UserStatus.ACTIVE);

        comment = Comment.builder()
                .reviewId(reviewId)
                .userId(userId)
                .content("test content")
                .build();

        commentDto = mock(CommentDto.class);
        findAllRequest = mock(CommentFindAllRequest.class);
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
        given(commentRepository.save(any(Comment.class))).willReturn(comment);
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
        given(userRepository.findById(eq(userId))).willReturn(Optional.of(user));
        given(commentRepository.findById(eq(commentId))).willReturn(Optional.empty());

        // when

        // then
        assertThatThrownBy(() -> commentService.updateComment(commentId, userId, request))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("댓글 수정 실패 - 작성자 불일치")
    void upDateCommentFailByAuthorMismatch() {
        // given
        UUID otherUserId = UUID.randomUUID();
        CommentUpdateRequest request = new CommentUpdateRequest("updated content");
        given(userRepository.findById(eq(otherUserId))).willReturn(Optional.of(user));
        given(commentRepository.findById(eq(commentId))).willReturn(Optional.of(comment));

        // when

        // then
        assertThatThrownBy(() -> commentService.updateComment(commentId, otherUserId, request))
                .isInstanceOf(CommentAuthorException.class);
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

    @Test
    @DisplayName("댓글 목록 조회 성공")
    void findAllCommentsCommentSuccess() {
        // given
        UUID secondUserId = UUID.randomUUID();
        UUID secondCommentId = UUID.randomUUID();

        User secondUser = mock(User.class);
        when(secondUser.getId()).thenReturn(secondUserId);
        when(secondUser.getNickname()).thenReturn("secondUser");

        Comment secondComment = Comment.builder()
                .reviewId(reviewId)
                .userId(secondUserId)
                .content("second content")
                .build();

        ReflectionTestUtils.setField(secondComment, "id", secondCommentId);
        ReflectionTestUtils.setField(secondComment, "createdAt", LocalDateTime.of(2026, 4, 21, 9, 59, 0));

        CommentDto firstCommentDto = mock(CommentDto.class);
        CommentDto secondCommentDto = mock(CommentDto.class);

        given(findAllRequest.reviewId()).willReturn(reviewId);
        given(findAllRequest.limit()).willReturn(2);

        given(reviewRepository.existsById(eq(reviewId))).willReturn(true);
        given(commentRepository.findAllByCursor(eq(findAllRequest)))
                .willReturn(List.of(comment, secondComment));
        given(commentRepository.countByReviewId(eq(reviewId))).willReturn(2);
        given(userRepository.findAllById(any()))
                .willReturn(List.of(user, secondUser));

        given(commentMapper.toDto(comment, userNickName)).willReturn(firstCommentDto);
        given(commentMapper.toDto(secondComment, "secondUser")).willReturn(secondCommentDto);

        // when
        CursorPageResponseCommentDto result = commentService.findAllComments(findAllRequest);

        // then
        assertThat(result.content()).isEqualTo(List.of(firstCommentDto, secondCommentDto));
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.totalElements()).isEqualTo(2);
        assertThat(result.hasNext()).isEqualTo(false);
        assertThat(result.nextCursor()).isEqualTo(null);
        assertThat(result.nextAfter()).isEqualTo(null);

        verify(reviewRepository).existsById(reviewId);
        verify(commentRepository).findAllByCursor(findAllRequest);
        verify(commentRepository).countByReviewId(reviewId);
        verify(userRepository).findAllById(any());
        verify(commentMapper).toDto(comment, userNickName);
        verify(commentMapper).toDto(secondComment, "secondUser");
    }

    @Test
    @DisplayName("댓글 목록 조회 실패 - 리뷰 정보 없음")
    void findAllCommentsCommentFailByReviewNotFound() {
        // given
        given(findAllRequest.reviewId()).willReturn(reviewId);
        given(findAllRequest.limit()).willReturn(10);

        given(reviewRepository.existsById(eq(reviewId))).willReturn(false);

        // when & then
        assertThatThrownBy(() -> commentService.findAllComments(findAllRequest))
                .isInstanceOf(EntityNotFoundException.class);

        verify(reviewRepository).existsById(reviewId);
        verify(commentRepository, never()).findAllByCursor(any());
    }

    @Test
    @DisplayName("댓글 논리 삭제 성공")
    void softDeleteCommentSuccess() {
        // given
        given(commentRepository.findById(eq(commentId))).willReturn(Optional.of(comment));
        given(userRepository.findById(eq(userId))).willReturn(Optional.of(user));
        given(commentRepository.save(any(Comment.class))).willReturn(comment);

        // when
        commentService.softDelete(commentId, userId);

        // then
        verify(commentRepository).findById(commentId);
        verify(userRepository).findById(userId);
        verify(commentRepository).save(comment);

    }

    @Test
    @DisplayName("댓글 논리 삭제 실패")
    void softDeleteCommentFail() {
        // given
        UUID otherUserId = UUID.randomUUID();

        User otherUser = mock(User.class);
        when(otherUser.getId()).thenReturn(otherUserId);
        when(otherUser.getNickname()).thenReturn("otherUser");

        given(commentRepository.findById(eq(commentId))).willReturn(Optional.of(comment));
        given(userRepository.findById(eq(otherUserId))).willReturn(Optional.of(otherUser));

        // when

        // then
        assertThatThrownBy(() -> commentService.softDelete(commentId, otherUserId))
                .isInstanceOf(CommentAuthorException.class);

        verify(commentRepository).findById(commentId);
        verify(userRepository).findById(otherUserId);
        verify(commentRepository, never()).save(any(Comment.class));

    }

    @Test
    @DisplayName("댓글 물리 삭제 성공")
    void hardDeleteCommentSuccess() {
        // given
        given(commentRepository.findById(eq(commentId))).willReturn(Optional.of(comment));
        given(userRepository.findById(eq(userId))).willReturn(Optional.of(user));

        // when
        commentService.hardDelete(commentId, userId);

        // then
        verify(commentRepository).findById(commentId);
        verify(userRepository).findById(userId);
        verify(commentRepository).deleteById(commentId);
    }

    @Test
    @DisplayName("댓글 물리 삭제 실패")
    void hardDeleteCommentFail() {
        // given
        UUID otherUserId = UUID.randomUUID();

        User otherUser = mock(User.class);
        when(otherUser.getId()).thenReturn(otherUserId);
        when(otherUser.getNickname()).thenReturn("otherUser");

        given(commentRepository.findById(eq(commentId))).willReturn(Optional.of(comment));
        given(userRepository.findById(eq(otherUserId))).willReturn(Optional.of(otherUser));

        // when

        // then
        assertThatThrownBy(() -> commentService.hardDelete(commentId, otherUserId))
                .isInstanceOf(CommentAuthorException.class);

        verify(commentRepository).findById(commentId);
        verify(userRepository).findById(otherUserId);
        verify(commentRepository, never()).deleteById(any(UUID.class));

    }
}
