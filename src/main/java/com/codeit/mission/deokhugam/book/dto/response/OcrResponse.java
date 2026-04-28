package com.codeit.mission.deokhugam.book.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "OCR API 응답")
public record OcrResponse(
    @Schema(
        description = "인식된 결과 목록"
    )
    @JsonProperty("ParsedResults") List<ParsedResult> parsedResults
) {

  public record ParsedResult(
      @Schema(
          description = "인식된 전체 텍스트 내용",
          example = "string"
      )
      @JsonProperty("ParsedText") String parsedText
  ) {

  }
}
