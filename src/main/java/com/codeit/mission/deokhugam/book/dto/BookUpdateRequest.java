package com.codeit.mission.deokhugam.book.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record BookUpdateRequest(
    //제목
    @NotBlank(message = "제목을 입력해주세요")
    String title,

    //지은이
    @NotBlank(message = "지은이를 입력해주세요.")
    String author,

    //책에 대한 설명
    @NotBlank(message = "설명을 최소 1글자 이상 적어주세요.")
    String description,

    //출판사
    @NotBlank(message = "출판사 정보를 입력해주세요.")
    String publisher,

    //출판일
    @NotNull(message = "출판 일자를 적어주세요.")
    LocalDate publishedDate
) {

}
