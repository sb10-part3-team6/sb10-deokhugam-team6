package com.codeit.mission.deokhugam.review.repository;

import com.codeit.mission.deokhugam.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

/*
    리뷰 레파지토리
 */
public interface ReviewRepository extends JpaRepository<Review, UUID> {
    // 중복 검사: 특정 도서에 대한 사용자 리뷰 존재 유무
    boolean existsByBookIdAndUserId(UUID bookId, UUID userId);

    // 특정 리뷰에 대한 특정 유저의 좋아요 여부
    @Query("SELECT COUNT(review.id) > 0 " +                                         // 조건 만족 여부에 따라, true / false 반환
            "FROM Review review " +
            "JOIN review.likedUsers user " +                                        // Review 엔티티 내부 likedUser 필드 조인
            "WHERE review.id = :reviewId AND user.id = :userId")                    // 리뷰 id 및 사용자 id에 대한 완전 일치 조건
    boolean existsLikedByIdAndUserId(@Param("reviewId") UUID reviewId,
                                     @Param("userId") UUID userId);
}
