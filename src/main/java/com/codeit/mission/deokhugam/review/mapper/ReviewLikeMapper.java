package com.codeit.mission.deokhugam.review.mapper;

import com.codeit.mission.deokhugam.review.dto.response.ReviewLikeDto;
import com.codeit.mission.deokhugam.review.entity.Review;
import com.codeit.mission.deokhugam.review.entity.ReviewLike;
import com.codeit.mission.deokhugam.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ReviewLikeMapper {

  // 엔티티 변환
  @Mapping(target = "review", source = "review")
  @Mapping(target = "user", source = "user")
  @Mapping(target = "likedAt", ignore = true)
  // @PrePersist를 통해 채워짐
  ReviewLike toEntity(Review review, User user);

  // 엔티티 -> 응답 DTO 변환
  @Mapping(target = "reviewId", source = "review.id")
  @Mapping(target = "userId", source = "user.id")
  @Mapping(target = "liked", source = "isLiked")
  ReviewLikeDto toDto(Review review, User user, boolean isLiked);
}
