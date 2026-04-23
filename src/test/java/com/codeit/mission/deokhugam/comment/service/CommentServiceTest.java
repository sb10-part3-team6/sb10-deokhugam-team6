package com.codeit.mission.deokhugam.comment.service;

import com.codeit.mission.deokhugam.comment.dto.request.CommentCreateRequest;
import com.codeit.mission.deokhugam.comment.dto.request.CommentFindAllRequest;
import com.codeit.mission.deokhugam.comment.dto.request.CommentUpdateRequest;
import com.codeit.mission.deokhugam.comment.dto.response.CommentDto;
import com.codeit.mission.deokhugam.comment.dto.response.CursorPageResponseCommentDto;
import com.codeit.mission.deokhugam.comment.entity.Comment;
import com.codeit.mission.deokhugam.comment.entity.CommentStatus;
import com.codeit.mission.deokhugam.comment.exception.CommentAuthorException;
import com.codeit.mission.deokhugam.comment.exception.CommentNotFoundException;
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
  private Review review;
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

    review = mock(Review.class);
    when(review.getId()).thenReturn(reviewId);
    when(review.getStatus()).thenReturn(ReviewStatus.ACTIVE);

    comment = Comment.builder()
            .reviewId(reviewId)
            .userId(userId)
            .content("test content")
            .status(CommentStatus.ACTIVE)
            .build();

    ReflectionTestUtils.setField(comment, "id", commentId);
    ReflectionTestUtils.setField(
            comment,
            "createdAt",
            LocalDateTime.of(2026, 4, 21, 10, 0, 0)
    );

    commentDto = mock(CommentDto.class);
    findAllRequest = mock(CommentFindAllRequest.class);
  }

  @Test
  @DisplayName("댓글 생성 성공")
  void createCommentSuccess() {
    // given
    CommentCreateRequest request = new CommentCreateRequest(reviewId, userId, "test content");

    Comment savedComment = Comment.builder()
            .reviewId(reviewId)
            .userId(userId)
            .content(request.content())
            .status(CommentStatus.ACTIVE)
            .build();

    given(reviewRepository.findById(eq(reviewId))).willReturn(Optional.of(review));
    given(userRepository.findById(eq(userId))).willReturn(Optional.of(user));
    given(commentRepository.saveAndFlush(any(Comment.class))).willReturn(savedComment);
    given(commentMapper.toDto(savedComment, userNickName)).willReturn(commentDto);

    // when
    CommentDto result = commentService.createComment(request);

    // then
    assertThat(result).isEqualTo(commentDto);
    verify(reviewRepository).findById(reviewId);
    verify(userRepository).findById(userId);
    verify(commentRepository).saveAndFlush(any(Comment.class));
    verify(reviewRepository).incrementCommentCount(reviewId);
    verify(commentMapper).toDto(savedComment, userNickName);
  }

  @Test
  @DisplayName("댓글 생성 실패 - 리뷰가 존재하지 않음")
  void createCommentFailByReviewNotFound() {
    // given
    CommentCreateRequest request = new CommentCreateRequest(reviewId, userId, "test content");
    given(reviewRepository.findById(eq(reviewId))).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> commentService.createComment(request))
            .isInstanceOf(ReviewNotFoundException.class);

    verify(reviewRepository).findById(reviewId);
    verify(userRepository, never()).findById(any());
    verify(commentRepository, never()).saveAndFlush(any());
    verify(reviewRepository, never()).incrementCommentCount(any());
  }

  @Test
  @DisplayName("댓글 생성 실패 - 유저가 삭제 상태")
  void createCommentFailByDeletedUser() {
    // given
    CommentCreateRequest request = new CommentCreateRequest(reviewId, userId, "test content");

    User deletedUser = mock(User.class);
    when(deletedUser.getId()).thenReturn(userId);
    when(deletedUser.getStatus()).thenReturn(UserStatus.DELETED);

    given(reviewRepository.findById(eq(reviewId))).willReturn(Optional.of(review));
    given(userRepository.findById(eq(userId))).willReturn(Optional.of(deletedUser));

    // when & then
    assertThatThrownBy(() -> commentService.createComment(request))
            .isInstanceOf(UserNotFoundException.class);

    verify(commentRepository, never()).saveAndFlush(any());
    verify(reviewRepository, never()).incrementCommentCount(any());
  }

  @Test
  @DisplayName("댓글 수정 성공")
  void updateCommentSuccess() {
    // given
    CommentUpdateRequest request = new CommentUpdateRequest("updated content");

    given(userRepository.findById(eq(userId))).willReturn(Optional.of(user));
    given(commentRepository.findById(eq(commentId))).willReturn(Optional.of(comment));
    given(commentRepository.save(any(Comment.class))).willReturn(comment);
    given(commentMapper.toDto(comment, userNickName)).willReturn(commentDto);

    // when
    CommentDto result = commentService.updateComment(commentId, userId, request);

    // then
    assertThat(result).isEqualTo(commentDto);
    assertThat(comment.getContent()).isEqualTo("updated content");
    verify(userRepository).findById(userId);
    verify(commentRepository).findById(commentId);
    verify(commentRepository).save(comment);
    verify(commentMapper).toDto(comment, userNickName);
  }

  @Test
  @DisplayName("댓글 수정 실패 - 댓글 존재하지 않음")
  void updateCommentFailByCommentNotFound() {
    // given
    CommentUpdateRequest request = new CommentUpdateRequest("updated content");

    given(userRepository.findById(eq(userId))).willReturn(Optional.of(user));
    given(commentRepository.findById(eq(commentId))).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> commentService.updateComment(commentId, userId, request))
            .isInstanceOf(CommentNotFoundException.class);

    verify(commentRepository, never()).save(any());
  }

  @Test
  @DisplayName("댓글 수정 실패 - 작성자 불일치")
  void updateCommentFailByAuthorMismatch() {
    // given
    UUID otherUserId = UUID.randomUUID();

    User otherUser = mock(User.class);
    when(otherUser.getId()).thenReturn(otherUserId);
    when(otherUser.getNickname()).thenReturn("otherUser");
    when(otherUser.getStatus()).thenReturn(UserStatus.ACTIVE);

    CommentUpdateRequest request = new CommentUpdateRequest("updated content");

    given(userRepository.findById(eq(otherUserId))).willReturn(Optional.of(otherUser));
    given(commentRepository.findById(eq(commentId))).willReturn(Optional.of(comment));

    // when & then
    assertThatThrownBy(() -> commentService.updateComment(commentId, otherUserId, request))
            .isInstanceOf(CommentAuthorException.class);

    verify(commentRepository, never()).save(any());
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
    verify(commentRepository).findById(commentId);
    verify(userRepository).findById(userId);
    verify(commentMapper).toDto(comment, userNickName);
  }

  @Test
  @DisplayName("댓글 상세 조회 실패 - 댓글 존재하지 않음")
  void findCommentFail() {
    // given
    given(commentRepository.findById(eq(commentId))).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> commentService.findComment(commentId))
            .isInstanceOf(CommentNotFoundException.class);

    verify(userRepository, never()).findById(any());
  }

  @Test
  @DisplayName("댓글 목록 조회 성공 - 마지막 페이지")
  void findAllCommentsSuccessLastPage() {
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
            .status(CommentStatus.ACTIVE)
            .build();

    ReflectionTestUtils.setField(secondComment, "id", secondCommentId);
    ReflectionTestUtils.setField(
            secondComment,
            "createdAt",
            LocalDateTime.of(2026, 4, 21, 9, 59, 0)
    );

    CommentDto firstCommentDto = mock(CommentDto.class);
    CommentDto secondCommentDto = mock(CommentDto.class);

    given(findAllRequest.reviewId()).willReturn(reviewId);
    given(findAllRequest.limit()).willReturn(2);

    given(reviewRepository.findById(eq(reviewId))).willReturn(Optional.of(review));
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
    assertThat(result.hasNext()).isFalse();
    assertThat(result.nextCursor()).isNull();
    assertThat(result.nextAfter()).isNull();
  }

  @Test
  @DisplayName("댓글 목록 조회 성공 - 다음 페이지 존재")
  void findAllCommentsSuccessHasNext() {
    // given
    UUID secondUserId = UUID.randomUUID();
    UUID thirdUserId = UUID.randomUUID();

    User secondUser = mock(User.class);
    when(secondUser.getId()).thenReturn(secondUserId);
    when(secondUser.getNickname()).thenReturn("secondUser");

    User thirdUser = mock(User.class);
    when(thirdUser.getId()).thenReturn(thirdUserId);
    when(thirdUser.getNickname()).thenReturn("thirdUser");

    Comment secondComment = Comment.builder()
            .reviewId(reviewId)
            .userId(secondUserId)
            .content("second content")
            .status(CommentStatus.ACTIVE)
            .build();

    Comment thirdComment = Comment.builder()
            .reviewId(reviewId)
            .userId(thirdUserId)
            .content("third content")
            .status(CommentStatus.ACTIVE)
            .build();

    UUID secondCommentId = UUID.randomUUID();
    UUID thirdCommentId = UUID.randomUUID();

    ReflectionTestUtils.setField(secondComment, "id", secondCommentId);
    ReflectionTestUtils.setField(thirdComment, "id", thirdCommentId);

    LocalDateTime secondCreatedAt = LocalDateTime.of(2026, 4, 21, 9, 59, 0);
    LocalDateTime thirdCreatedAt = LocalDateTime.of(2026, 4, 21, 9, 58, 0);

    ReflectionTestUtils.setField(secondComment, "createdAt", secondCreatedAt);
    ReflectionTestUtils.setField(thirdComment, "createdAt", thirdCreatedAt);

    CommentDto firstCommentDto = mock(CommentDto.class);
    CommentDto secondCommentDto = mock(CommentDto.class);

    given(findAllRequest.reviewId()).willReturn(reviewId);
    given(findAllRequest.limit()).willReturn(2);

    given(reviewRepository.findById(eq(reviewId))).willReturn(Optional.of(review));
    given(commentRepository.findAllByCursor(eq(findAllRequest)))
            .willReturn(List.of(comment, secondComment, thirdComment));
    given(commentRepository.countByReviewId(eq(reviewId))).willReturn(3);
    given(userRepository.findAllById(any()))
            .willReturn(List.of(user, secondUser, thirdUser));

    given(commentMapper.toDto(comment, userNickName)).willReturn(firstCommentDto);
    given(commentMapper.toDto(secondComment, "secondUser")).willReturn(secondCommentDto);

    // when
    CursorPageResponseCommentDto result = commentService.findAllComments(findAllRequest);

    // then
    assertThat(result.content()).isEqualTo(List.of(firstCommentDto, secondCommentDto));
    assertThat(result.size()).isEqualTo(2);
    assertThat(result.totalElements()).isEqualTo(3);
    assertThat(result.hasNext()).isTrue();
    assertThat(result.nextCursor()).isEqualTo(secondCommentId.toString());
    assertThat(result.nextAfter()).isEqualTo(secondCreatedAt);
  }

  @Test
  @DisplayName("댓글 목록 조회 실패 - 리뷰 정보 없음")
  void findAllCommentsFailByReviewNotFound() {
    // given
    given(findAllRequest.reviewId()).willReturn(reviewId);
    given(reviewRepository.findById(eq(reviewId))).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> commentService.findAllComments(findAllRequest))
            .isInstanceOf(ReviewNotFoundException.class);

    verify(commentRepository, never()).findAllByCursor(any());
  }

  @Test
  @DisplayName("댓글 논리 삭제 성공")
  void softDeleteCommentSuccess() {
    // given
    given(commentRepository.findById(eq(commentId))).willReturn(Optional.of(comment));
    given(userRepository.findById(eq(userId))).willReturn(Optional.of(user));

    // when
    commentService.softDelete(commentId, userId);

    // then
    assertThat(comment.getStatus()).isEqualTo(CommentStatus.DELETED);
    verify(commentRepository).findById(commentId);
    verify(userRepository).findById(userId);
    verify(reviewRepository).decrementCommentCount(reviewId);
    verify(commentRepository, never()).deleteById(any(UUID.class));
  }

  @Test
  @DisplayName("댓글 논리 삭제 실패 - 작성자 불일치")
  void softDeleteCommentFail() {
    // given
    UUID otherUserId = UUID.randomUUID();

    User otherUser = mock(User.class);
    when(otherUser.getId()).thenReturn(otherUserId);
    when(otherUser.getStatus()).thenReturn(UserStatus.ACTIVE);

    given(commentRepository.findById(eq(commentId))).willReturn(Optional.of(comment));
    given(userRepository.findById(eq(otherUserId))).willReturn(Optional.of(otherUser));

    // when & then
    assertThatThrownBy(() -> commentService.softDelete(commentId, otherUserId))
            .isInstanceOf(CommentAuthorException.class);

    verify(commentRepository).findById(commentId);
    verify(userRepository).findById(otherUserId);
    verify(reviewRepository, never()).decrementCommentCount(any());
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
    verify(reviewRepository).decrementCommentCount(reviewId);
  }

  @Test
  @DisplayName("댓글 물리 삭제 실패 - 작성자 불일치")
  void hardDeleteCommentFail() {
    // given
    UUID otherUserId = UUID.randomUUID();

    User otherUser = mock(User.class);
    when(otherUser.getId()).thenReturn(otherUserId);
    when(otherUser.getStatus()).thenReturn(UserStatus.ACTIVE);

    given(commentRepository.findById(eq(commentId))).willReturn(Optional.of(comment));
    given(userRepository.findById(eq(otherUserId))).willReturn(Optional.of(otherUser));

    // when & then
    assertThatThrownBy(() -> commentService.hardDelete(commentId, otherUserId))
            .isInstanceOf(CommentAuthorException.class);

    verify(commentRepository).findById(commentId);
    verify(userRepository).findById(otherUserId);
    verify(commentRepository, never()).deleteById(any(UUID.class));
    verify(reviewRepository, never()).decrementCommentCount(any());
  }
}