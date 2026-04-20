package com.codeit.mission.deokhugam.book.dto;

import java.util.List;

public record OcrResponse(
        List<ParsedResult> ParsedResults
) {
    public record ParsedResult(
            String ParsedText
    ) {}
}
