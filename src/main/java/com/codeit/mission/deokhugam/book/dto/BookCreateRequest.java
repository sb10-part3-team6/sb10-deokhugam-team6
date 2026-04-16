package com.codeit.mission.deokhugam.book.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record BookCreateRequest(
        //제목
        @NotBlank
        String title,

        //지은이
        @NotBlank
        String author,

        //책에 대한 설명
        @NotBlank
        String description,

        //출판사
        @NotBlank
        String publisher,

        //출판일
        @NotNull
        LocalDate publishedDate,

        //ISBN 값
        String isbn
) {
}
