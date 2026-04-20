package com.codeit.mission.deokhugam.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.codeit.mission.deokhugam.book.entity.Book;
import com.codeit.mission.deokhugam.notification.entity.Notification;
import com.codeit.mission.deokhugam.notification.repository.NotificationRepository;
import com.codeit.mission.deokhugam.review.entity.Review;
import com.codeit.mission.deokhugam.review.repository.ReviewRepository;
import com.codeit.mission.deokhugam.user.entity.User;
import com.codeit.mission.deokhugam.user.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private NotificationService notificationService;


    @Test
    @DisplayName("좋아요에 의한 알림 생성 테스트")
    void createNotificationByLike() {
        // given
        UUID actorId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        User actor = createUser("좋아요 유저");
        User receiver = createUser("작성자");
        Review review = createReview(receiver);

        given(userRepository.findById(actorId)).willReturn(Optional.of(actor));
        given(userRepository.findById(receiverId)).willReturn(Optional.of(receiver));
        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));

        // when
        notificationService.createByLike(actorId, receiverId, reviewId);

        // then
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(1)).save(captor.capture());
        verifyNoMoreInteractions(notificationRepository);

        Notification saved = captor.getValue();

        assertThat(saved.getUser()).isSameAs(receiver);   // 동일 객체인지
        assertThat(saved.getReview()).isSameAs(review);
        assertThat(saved.getReviewContent()).isEqualTo("리뷰 내용");
        assertThat(saved.isConfirmed()).isFalse();
        assertThat(saved.getMessage())
            .isEqualTo("[" + actor.getNickname() + "]님이 나의 리뷰를 좋아합니다.");
    }

    @Test
    @DisplayName("댓글에 의한 알림 생성 테스트")
    void createNotificationByComment() {
        // given
        UUID actorId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        User actor = createUser("댓글 유저");
        User receiver = createUser("작성자");
        Review review = createReview(receiver);

        given(userRepository.findById(actorId)).willReturn(Optional.of(actor));
        given(userRepository.findById(receiverId)).willReturn(Optional.of(receiver));
        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));

        // when
        notificationService.createByComment(actorId, receiverId, reviewId);

        // then
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification saved = captor.getValue();

        assertThat(saved.getUser()).isSameAs(receiver);   // 동일 객체인지
        assertThat(saved.getReview()).isSameAs(review);
        assertThat(saved.getReviewContent()).isEqualTo("리뷰 내용");
        assertThat(saved.isConfirmed()).isFalse();
        assertThat(saved.getMessage())
            .isEqualTo("[" + actor.getNickname() + "]님이 나의 리뷰에 댓글을 남겼습니다.");
    }

    @Test
    @DisplayName("리뷰 랭킹 선정에 의한 알림 생성 테스트")
    void createNotificationByReviewRanked() {
        // given
        UUID receiverId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        User receiver = createUser("작성자");
        Review review = createReview(receiver);

        given(userRepository.findById(receiverId)).willReturn(Optional.of(receiver));
        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));

        // when
        notificationService.createByReviewRanked(receiverId, reviewId);

        // then
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification saved = captor.getValue();

        assertThat(saved.getUser()).isSameAs(receiver);   // 동일 객체인지
        assertThat(saved.getReview()).isSameAs(review);
        assertThat(saved.getReviewContent()).isEqualTo("리뷰 내용");
        assertThat(saved.isConfirmed()).isFalse();
        assertThat(saved.getMessage())
            .isEqualTo("나의 리뷰가 인기 리뷰로 등록되었습니다.");
    }

    @Test
    @DisplayName("actor 유저 정보 없음 실패")
    void createNotificationActorUserNotFound() {
        // given
        UUID actorId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        given(userRepository.findById(actorId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
            notificationService.createByLike(actorId, receiverId, reviewId)
        ).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("receiver 유저 정보 없음 실패")
    void createNotificationReceiverUserNotFound() {
        // given
        UUID actorId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        User actor = createUser("actor");

        given(userRepository.findById(actorId)).willReturn(Optional.of(actor));
        given(userRepository.findById(receiverId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
            notificationService.createByLike(actorId, receiverId, reviewId)
        ).isExactlyInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("리뷰 정보 없음 실패")
    void createNotificationReviewNotFound() {
        // given
        UUID actorId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        User actor = createUser("작성자");
        User receiver = createUser("작성자");
        Review review = createReview(receiver);

        given(userRepository.findById(actorId)).willReturn(Optional.of(actor));
        given(userRepository.findById(receiverId)).willReturn(Optional.of(receiver));
        given(reviewRepository.findById(reviewId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
            notificationService.createByLike(actorId, receiverId, reviewId)
        ).isInstanceOf(RuntimeException.class);
    }

    private User createUser(String nickname) {
        return User.builder()
            .nickname(nickname)
            .build();
    }

    private Review createReview(User user) {
        Book book = Book.builder()
            .title("제목")
            .rating(5)
            .build();

        return Review.builder()
            .user(user)
            .book(book)
            .content("리뷰 내용")
            .rating(5)
            .build();
    }
}
