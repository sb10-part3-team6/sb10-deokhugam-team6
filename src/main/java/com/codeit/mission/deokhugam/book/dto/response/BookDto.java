package com.codeit.mission.deokhugam.book.dto.response;

import java.time.Instant;
import java.util.UUID;

public record BookDto(
    UUID id,
    String title,
    String author,
    String description,
    String publisher,
    Instant publishedDate,
    String isbn,
    String thumbnailUrl,
    int reviewCount,
    double rating,
    Instant createdAt,
    Instant updatedAt
) {

}
