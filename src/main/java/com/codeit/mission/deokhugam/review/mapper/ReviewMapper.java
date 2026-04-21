package com.codeit.mission.deokhugam.review.mapper;

import com.codeit.mission.deokhugam.review.dto.response.CursorPageResponseReviewDto;
import com.codeit.mission.deokhugam.review.dto.response.ReviewDto;
import com.codeit.mission.deokhugam.review.entity.Review;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
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

  // Entity List -> 응답 DTO List
  default List<ReviewDto> toDtoList(List<Review> reviews, List<UUID> likedReviewIds) {
    if (reviews == null) {
      return Collections.emptyList();
    }

    return reviews.stream()
        .map(review -> toDto(review,
            likedReviewIds != null && likedReviewIds.contains(review.getId())))
        .toList();
  }

  // 응답 DTO -> 페이징 전용 응답 DTO 변환
  default CursorPageResponseReviewDto<ReviewDto> toCursorPageResponse(
      List<ReviewDto> content,
      String nextCursor,
      LocalDateTime nextAfter,
      int limit,
      long totalElements,
      boolean hasNext
  ) {
    return CursorPageResponseReviewDto.<ReviewDto>builder()
        .content(content)
        .nextCursor(nextCursor)
        .nextAfter(nextAfter)
        .size(limit)
        .totalElements(totalElements)
        .hasNext(hasNext)
        .build();
  }
}
