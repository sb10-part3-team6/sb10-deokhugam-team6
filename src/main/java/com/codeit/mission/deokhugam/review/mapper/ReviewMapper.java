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
    @Mapping(target = "id", source = "review.id")
    @Mapping(target = "bookId", source = "review.book.id")
    @Mapping(target = "bookTitle", source = "review.book.title")
    @Mapping(target = "bookThumbnailUrl", source = "review.book.thumbnailUrl")
    @Mapping(target = "userId", source = "review.user.id")
    @Mapping(target = "userNickName", source = "review.user.nickname")
    @Mapping(target = "likedByMe", source = "isLiked")
    ReviewDto toDto(Review review, boolean isLiked);
}
