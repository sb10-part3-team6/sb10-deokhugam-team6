package com.codeit.mission.deokhugam.review.repository;

import com.codeit.mission.deokhugam.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/*
    리뷰 레파지토리
 */
public interface ReviewRepository extends JpaRepository<Review, UUID> {
    // 중복 검사: 특정 도서에 대한 사용자 리뷰 존재 유무
    boolean existsByBookIdAndUserId(UUID bookId, UUID userId);
}
