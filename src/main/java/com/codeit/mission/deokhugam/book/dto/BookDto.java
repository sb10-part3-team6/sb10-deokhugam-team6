package com.codeit.mission.deokhugam.book.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record BookDto(
    UUID id,
    String title,
    String author,
    String description,
    String publisher,
    LocalDate publishedDate,
    String isbn,
    String thumbnailUrl,
    int reviewCount,
    BigDecimal rating,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
