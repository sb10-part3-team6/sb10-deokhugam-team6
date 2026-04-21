package com.codeit.mission.deokhugam.review.repository;

import com.codeit.mission.deokhugam.review.entity.ReviewLike;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewLikeRepository extends JpaRepository <ReviewLike, UUID> {
  public void deleteByUserIdAndReviewId(UUID userId, UUID reviewId);

  public ReviewLike findByUserIdAndReviewId(UUID userId, UUID reviewId);
}
