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
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
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
public class NotificationRepositoryTest {

  @Autowired
  private NotificationRepository notificationRepository;

  @Autowired
  private EntityManager em;

  private User user;
  private Book book;
  private Review review;
  private List<Notification> notificationList;

  @BeforeEach
  void setUp() {
    LocalDateTime BASE_TIME =
      LocalDateTime.of(2026, 1, 1, 0, 0, 0);

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

    notificationList = new ArrayList<>();
    for (int i = 0; i < 30; i++) {
      Notification notification = Notification.builder()
        .user(user)
        .message(i + "번째 알림")
        .review(review)
        .reviewContent(review.getContent())
        .message("알림 메시지 " + i)
        .confirmed(i % 2 == 0)
        .build();

      // reflection으로 createAt 강제 세팅
      ReflectionTestUtils.setField(
        notification,
        "createdAt",
        BASE_TIME.minusSeconds(i)
      );

      notificationList.add(notification);
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

    Instant cursor = firstPage.getContent().get(19)
      .getCreatedAt()
      .toInstant(ZoneOffset.UTC);

    NotificationRequestQuery secondQuery = NotificationRequestQuery.builder()
      .direction(DirectionEnum.DESC)
      .after(cursor)
      .build();

    // when
    Slice<Notification> secondPage =
      notificationRepository.findByUserWithCursor(user.getId(), secondQuery);

    // then
    assertThat(secondPage.getContent()).hasSize(10);

    secondPage.getContent().forEach(n -> {
        Instant createdAt = n.getCreatedAt().toInstant(ZoneOffset.UTC);
        assertThat(createdAt).isBefore(cursor);
      }
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

  @Test
  @DisplayName("사용자 알림 총 개수 조회 확인")
  void countByUserIdSuccess() {
    long count = notificationRepository.countByUserId(user.getId());
    assertThat(count).isEqualTo(30L);
  }

  @Test
  @DisplayName("사용자 알림 전체 읽음 처리")
  void updateAllAsConfirmed_Success() {
    // when
    int updatedNotificationCount = notificationRepository.updateAllAsConfirmed(user.getId());

    em.flush();
    em.clear(); // 벌크 연산 후 반드시 clear를 해야한다.

    // then
    // 갱신된 row의 개수 검증
    assertThat(updatedNotificationCount).isEqualTo(notificationList.size() / 2);

    // 실제로 confirm이 true로 바뀌었는지 검증
    List<Notification> notifications = em
      .createQuery("select n from Notification n where n.user.id = :userId", Notification.class)
      .setParameter("userId", user.getId())
      .getResultList();

    assertThat(notifications)
      .allMatch(Notification::isConfirmed);

  }
}
