package com.codeit.mission.deokhugam.review.entity;

import com.codeit.mission.deokhugam.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/*
    Review
    -------
    사용자가 도서 별 하나씩 작성한 리뷰 정보
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "reviews",                                           // 데이터베이스 테이블 이름
        uniqueConstraints = {                                       // 중복 방지를 위한 제약 조건 설정
                @UniqueConstraint(
                        name = "uk_book_user",
                        columnNames = {"book_id", "user_id"}
                )
        }
)
public class Review extends BaseEntity {
    @Column(name = "book_id", nullable = false)
    private UUID bookId;                                        // 리븊 대상 도서

    @Column(name = "user_id", nullable = false)
    private UUID userId;                                        // 리뷰 작성자

    @Column(nullable = false, length = 500)
    private String content;                                     // 리뷰 내용 (최댓값: 500)

    @Column(nullable = false)
    private int rating;                                         // 리뷰 평점

    @Column(nullable = false)
    private Long likesCount = 0L;                               // 리뷰의 좋아요 수 (기본값: 0)

    @Column(nullable = false)
    private Long commentsCount = 0L;                            // 리뷰의 댓글 수 (기본값: 0)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewStatus status = ReviewStatus.ACTIVE;          // 리뷰 상태 (기본값: 활성)

    private LocalDateTime deletedAt;                                  // 리뷰 논리 삭제 시점

    // 생성자
    @Builder
    public Review(UUID bookId, UUID userId, Integer rating, String content) {
        this.bookId = bookId;
        this.userId = userId;
        this.rating = rating;
        this.content = content;
    }

    // 리뷰 논리 삭제
    public void delete(){
        this.status = ReviewStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
    }

    // 좋아요 수 증가
    public void incrementLikesCount() {
        this.likesCount += 1L;
    }

    // 좋아요 수 감소
    public void decrementLikesCount() {
        if (this.likesCount > 0) {
            this.likesCount -= 1L;
        }
    }

    // 댓글 수 증가
    public void incrementCommentsCount() {
        this.commentsCount += 1L;
    }

    // 댓글 수 감소
    public  void decrementCommentsCount() {
        if (this.commentsCount > 0) {
            this.commentsCount -= 1L;
        }
    }
}

