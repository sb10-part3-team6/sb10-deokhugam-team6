package com.codeit.mission.deokhugam.book.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "네이버 도서 API 응답")
public record NaverResponseDto(
    List<Item> items
) {

  public record Item(
      @Schema(
          description = "도서명",
          example = "string"
      )
      String title,

      @Schema(
          description = "URL 주소",
          example = "string"
      )
      String link,

      @Schema(
          description = "이미지",
          example = "string"
      )
      String image,

      @Schema(
          description = "저자",
          example = "string"
      )
      String author,

      @Schema(
          description = "판매 가격",
          example = "20000"
      )
      String discount,

      @Schema(
          description = "출판사",
          example = "string"
      )
      String publisher,

      @Schema(
          description = "출판일",
          example = "20260427"
      )
      String pubdate,

      @Schema(
          description = "ISBN",
          example = "string"
      )
      String isbn,

      @Schema(
          description = "설명",
          example = "string"
      )
      String description
  ) {

  }
}
