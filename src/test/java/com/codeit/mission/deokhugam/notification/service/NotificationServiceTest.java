package com.codeit.mission.deokhugam.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.codeit.mission.deokhugam.book.entity.Book;
import com.codeit.mission.deokhugam.notification.dto.CursorPageResponseNotificationDto;
import com.codeit.mission.deokhugam.notification.dto.NotificationDto;
import com.codeit.mission.deokhugam.notification.dto.NotificationRequestQuery;
import com.codeit.mission.deokhugam.notification.entity.Notification;
import com.codeit.mission.deokhugam.notification.mapper.NotificationMapper;
import com.codeit.mission.deokhugam.notification.repository.NotificationRepository;
import com.codeit.mission.deokhugam.review.entity.Review;
import com.codeit.mission.deokhugam.review.exception.ReviewNotFoundException;
import com.codeit.mission.deokhugam.review.repository.ReviewRepository;
import com.codeit.mission.deokhugam.user.entity.User;
import com.codeit.mission.deokhugam.user.exception.UserNotFoundException;
import com.codeit.mission.deokhugam.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Spy
    private NotificationMapper notificationMapper;

    @InjectMocks
    private NotificationService notificationService;

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

    @Nested
    @DisplayName("알림 등록")
    class RegistNotificationTest {

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
            ).isInstanceOf(UserNotFoundException.class);
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
            ).isExactlyInstanceOf(UserNotFoundException.class);
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
            ).isInstanceOf(ReviewNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("알림 목록 조회")
    class FindNotificationTest {

        private UUID userId;
        private NotificationRequestQuery query;

        @BeforeEach
        void setUp() {
            userId = UUID.randomUUID();

            query = NotificationRequestQuery.builder()
                .limit(2)
                .build();
        }

        @Test
        @DisplayName("첫번째 페이지 조회 성공")
        void findFirstPageSuccess() {
            // given
            Notification n1 = mock(Notification.class);
            Notification n2 = mock(Notification.class);

            LocalDateTime time1 = LocalDateTime.now();
            LocalDateTime time2 = time1.minusSeconds(10);

            given(n2.getCreatedAt()).willReturn(time2);

            List<Notification> notifications = List.of(n1, n2);

            Slice<Notification> slice =
                new SliceImpl<>(notifications, PageRequest.of(0, 2), true);

            given(notificationRepository.findByUserWithCursor(userId, query))
                .willReturn(slice);

            given(notificationRepository.countByUserId(userId))
                .willReturn(10L);

            NotificationDto dto1 = mock(NotificationDto.class);
            NotificationDto dto2 = mock(NotificationDto.class);

            given(notificationMapper.toDto(n1)).willReturn(dto1);
            given(notificationMapper.toDto(n2)).willReturn(dto2);

            // when
            CursorPageResponseNotificationDto result =
                notificationService.findByUserId(userId, query);

            // then
            assertThat(result.content()).hasSize(2);
            assertThat(result.size()).isEqualTo(2);
            assertThat(result.totalElements()).isEqualTo(10);
            assertThat(result.hasNext()).isTrue();
            assertThat(result.nextCursor()).isEqualTo(time2.toString());
            assertThat(result.nextAfter()).isEqualTo(time2);
        }

        @Test
        @DisplayName("조회 결과가 없을 시 nextCursor, nextAfter가 null인지 검증")
        void findByUserIdNoContent() {
            // given
            Slice<Notification> slice =
                new SliceImpl<>(List.of(), PageRequest.of(0, 2), false);

            given(notificationRepository.findByUserWithCursor(userId, query))
                .willReturn(slice);

            given(notificationRepository.countByUserId(userId))
                .willReturn(0L);

            // when
            CursorPageResponseNotificationDto result =
                notificationService.findByUserId(userId, query);

            // then
            assertThat(result.content()).isEmpty();
            assertThat(result.nextCursor()).isNull();
            assertThat(result.nextAfter()).isNull();
            assertThat(result.hasNext()).isFalse();
        }

    }


}
