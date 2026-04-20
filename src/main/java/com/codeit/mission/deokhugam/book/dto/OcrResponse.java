package com.codeit.mission.deokhugam.book.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record OcrResponse(
        @JsonProperty("ParsedResults") List<parsedResult> parsedResults
) {
    public record parsedResult(
            @JsonProperty("ParsedText") String parsedText
    ) {}
}
