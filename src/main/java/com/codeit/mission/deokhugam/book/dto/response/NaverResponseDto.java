package com.codeit.mission.deokhugam.book.dto.response;

import java.util.List;

//네이버에서 들어오는 응답 처리용
public record NaverResponseDto(
    List<Item> items
) {

  public record Item(
      String title,
      String link,
      String image,
      String author,
      String discount,
      String publisher,
      String pubdate,
      String isbn,
      String description
  ) {

  }
}
