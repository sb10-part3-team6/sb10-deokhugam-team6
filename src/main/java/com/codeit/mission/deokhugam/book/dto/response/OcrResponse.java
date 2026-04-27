package com.codeit.mission.deokhugam.book.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record OcrResponse(
    @JsonProperty("ParsedResults") List<ParsedResult> parsedResults
) {

  public record ParsedResult(
      @JsonProperty("ParsedText") String parsedText
  ) {

  }
}
