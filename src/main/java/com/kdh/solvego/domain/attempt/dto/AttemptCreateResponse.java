package com.kdh.solvego.domain.attempt.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "문제 풀이 제출 응답")
public record AttemptCreateResponse(

        @Schema(description = "정답 여부", example = "true")
        @JsonProperty("isCorrect")
        boolean isCorrect
) {
}