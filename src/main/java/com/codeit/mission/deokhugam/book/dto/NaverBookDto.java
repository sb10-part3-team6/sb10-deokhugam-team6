package com.codeit.mission.deokhugam.book.dto;

import lombok.Builder;

import java.time.LocalDate;

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
