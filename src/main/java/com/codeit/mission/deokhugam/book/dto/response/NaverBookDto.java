package com.codeit.mission.deokhugam.book.dto.response;

import java.time.LocalDate;
import lombok.Builder;

@Builder
public record NaverBookDto(
    String title,
    String author,
    String description,
    String publisher,
    LocalDate publishedDate,
    String isbn,
    byte[] thumbnailImage
) {

}
