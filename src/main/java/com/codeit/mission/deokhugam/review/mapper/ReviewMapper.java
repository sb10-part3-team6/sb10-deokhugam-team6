package com.codeit.mission.deokhugam.review.mapper;

import com.codeit.mission.deokhugam.review.dto.response.ReviewDto;
import com.codeit.mission.deokhugam.review.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/*
    리뷰 매퍼
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ReviewMapper {
    // Entity -> 응답 DTO 변환
    @Mapping(target = "likedByMe", source = "isLiked")
    ReviewDto toDto(Review review);
}
