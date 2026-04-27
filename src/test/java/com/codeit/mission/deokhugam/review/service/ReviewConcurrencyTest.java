package com.codeit.mission.deokhugam.review.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.codeit.mission.deokhugam.book.entity.Book;
import com.codeit.mission.deokhugam.book.repository.BookRepository;
import com.codeit.mission.deokhugam.review.entity.Review;
import com.codeit.mission.deokhugam.review.entity.ReviewStatus;
import com.codeit.mission.deokhugam.review.repository.ReviewLikeRepository;
import com.codeit.mission.deokhugam.review.repository.ReviewRepository;
import com.codeit.mission.deokhugam.user.entity.User;
import com.codeit.mission.deokhugam.user.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

@SpringBootTest
public class ReviewConcurrencyTest {

  @Autowired
  private ReviewService reviewServiceImplement;

  @Autowired
  private ReviewRepository reviewRepository;

  @Autowired
  private ReviewLikeRepository reviewLikeRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private BookRepository bookRepository;

  // 테스트 직후 데이터 초기화
  @AfterEach
  public void tearDown() {
    reviewLikeRepository.deleteAllInBatch();
    reviewRepository.deleteAllInBatch();
    bookRepository.deleteAllInBatch();
    userRepository.deleteAllInBatch();
  }

  // [성공]
  @Test
  @DisplayName("동시성 제어: 서로 다른 100명의 사용자가 동시에 좋아요를 누른 경우, 좋아요 수로 100을 반환")
  void add_review_concurrent_success() throws InterruptedException {
    // given
    int threadCount = 100;

    // 가짜 객체 | 도서 및 저자
    User author = User.builder()
        .nickname("author")
        .email("author@test.com")
        .password("password")
        .build();
    userRepository.save(author);

    Book book = Book.builder()
        .title("book")
        .author(author.getNickname())
        .isbn("1234567890")
        .publishedDate(java.time.LocalDate.now())
        .publisher("codeit")
        .description("holymoly")
        .build();
    bookRepository.save(book);

    // 좋아요를 추가할 리뷰 정보
    Review savedReview = Review.builder()
        .book(book)
        .user(author)
        .content("돌덩이 외게인이 뭐가 재밌다고 난리야")
        .rating(3)
        .build();
    reviewRepository.save(savedReview);

    // 동시에 좋아요를 누를 사용자 목록
    List<User> likedUsers = new ArrayList<>();
    for (int i = 0; i < threadCount; i++) {
      User targetUser = User.builder()
          .nickname("user" + i)
          .email("user" + i + "@test.com")
          .password("password")
          .build();

      likedUsers.add(userRepository.save(targetUser));
    }

    // 32개 스레드가 동시 수행 가능한 환경 풀 (Pool)
    ExecutorService executorService = Executors.newFixedThreadPool(32);
    CountDownLatch latch = new CountDownLatch(threadCount);

    // when
    for (int i = 0; i < threadCount; i++) {
      User liker = likedUsers.get(i);

      executorService.submit(() -> {
        try {
          // 각 스레드가 동시에 좋아요 추가 요청 수행
          reviewServiceImplement.toggleLike(savedReview.getId(), liker.getId());
        } finally {
          // latch 카운트 1 감소
          latch.countDown();
        }
      });
    }

    // 모든 스레드 작업이 끝날 때까지 대기
    latch.await();

    // then
    Review updatedReview = reviewRepository.findById(savedReview.getId()).orElseThrow();
    assertEquals(threadCount, updatedReview.getLikeCount());
  }
}
