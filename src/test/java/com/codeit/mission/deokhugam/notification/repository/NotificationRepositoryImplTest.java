package com.codeit.mission.deokhugam.notification.repository;


import static org.assertj.core.api.Assertions.assertThat;

import com.codeit.mission.deokhugam.book.entity.Book;
import com.codeit.mission.deokhugam.config.QuerydslConfig;
import com.codeit.mission.deokhugam.dashboard.DirectionEnum;
import com.codeit.mission.deokhugam.notification.dto.NotificationRequestQuery;
import com.codeit.mission.deokhugam.notification.entity.Notification;
import com.codeit.mission.deokhugam.review.entity.Review;
import com.codeit.mission.deokhugam.user.entity.User;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Slice;
import org.springframework.test.util.ReflectionTestUtils;

@DataJpaTest
@Import(QuerydslConfig.class)
public class NotificationRepositoryImplTest {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private EntityManager em;

    private User user;
    private Book book;
    private Review review;

    @BeforeEach
    void setUp() {
        user = User.builder()
            .email("test@test.com")
            .nickname("test")
            .password("password")
            .build();

        book = Book.builder()
            .rating(5)
            .title("제목")
            .publishedDate(LocalDate.now())
            .author("작가")
            .publisher("출판사")
            .description("설명")
            .isbn("")
            .build();

        review = Review.builder()
            .rating(5)
            .user(user)
            .content("리뷰 내용")
            .book(book)
            .build();

        em.persist(user);
        em.persist(book);
        em.persist(review);

        for (int i = 0; i < 30; i++) {
            Notification notification = Notification.builder()
                .user(user)
                .message(i + "번째 알림")
                .review(review)
                .reviewContent(review.getContent())
                .message("알림 메시지 " + i)
                .build();

            // reflection으로 createAt 강제 세팅
            ReflectionTestUtils.setField(
                notification,
                "createdAt",
                LocalDateTime.now().minusSeconds(i)
            );

            em.persist(notification);
        }

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("최신순 조회 확인(desc, limit 20)")
    void findFirstPage() {
        // given
        UUID userId = user.getId();

        NotificationRequestQuery query = NotificationRequestQuery.builder()
            .direction(DirectionEnum.DESC)
            .build();

        // when
        Slice<Notification> result =
            notificationRepository.findByUserWithCursor(userId, query);

        // then
        assertThat(result.getContent()).hasSize(20);
        assertThat(result.hasNext()).isTrue();
    }

    @Test
    @DisplayName("커서를 기준으로 다음 페이지 조회 확인")
    void findNextPage() {
        // given
        NotificationRequestQuery firstQuery = NotificationRequestQuery.builder()
            .direction(DirectionEnum.DESC)
            .limit(20)
            .build();

        Slice<Notification> firstPage =
            notificationRepository.findByUserWithCursor(user.getId(), firstQuery);

        LocalDateTime cursor =
            firstPage.getContent().get(19).getCreatedAt();

        NotificationRequestQuery secondQuery = NotificationRequestQuery.builder()
            .direction(DirectionEnum.DESC)
            .after(cursor)
            .build();

        // when
        Slice<Notification> secondPage =
            notificationRepository.findByUserWithCursor(user.getId(), secondQuery);

        // then
        assertThat(secondPage.getContent()).hasSize(10);

        secondPage.getContent().forEach(n ->
            assertThat(n.getCreatedAt()).isBefore(cursor)
        );
    }

    @Test
    @DisplayName("마지막 페이지에서 hasNext가 false인지 검증")
    void findLastPage() {
        // given
        NotificationRequestQuery query = NotificationRequestQuery.builder()
            .direction(DirectionEnum.DESC)
            .limit(50)
            .build();

        // when
        Slice<Notification> result =
            notificationRepository.findByUserWithCursor(user.getId(), query);

        // then
        assertThat(result.hasNext()).isFalse();
    }

    @Test
    @DisplayName("오름차순 정렬 조회 확인")
    void findWithAsc() {
        // given
        NotificationRequestQuery query = NotificationRequestQuery.builder()
            .direction(DirectionEnum.ASC)
            .build();

        // when
        Slice<Notification> result =
            notificationRepository.findByUserWithCursor(user.getId(), query);

        // then
        List<Notification> content = result.getContent();

        for (int i = 0; i < content.size() - 1; i++) {
            assertThat(content.get(i).getCreatedAt())
                .isBeforeOrEqualTo(content.get(i + 1).getCreatedAt());
        }
    }
}
