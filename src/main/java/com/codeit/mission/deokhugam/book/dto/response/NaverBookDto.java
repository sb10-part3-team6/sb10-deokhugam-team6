package com.codeit.mission.deokhugam.book.dto.response;

import java.time.Instant;
import lombok.Builder;

@Builder
public record NaverBookDto(
    String title,
    String author,
    String description,
    String publisher,
    Instant publishedDate,
    String isbn,
    byte[] thumbnailImage
) {

}
