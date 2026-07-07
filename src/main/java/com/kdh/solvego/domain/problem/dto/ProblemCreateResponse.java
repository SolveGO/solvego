package com.kdh.solvego.domain.problem.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "문제 등록 응답")
public record ProblemCreateResponse(

        @Schema(description = "생성된 문제 ID", example = "1")
        Long problemId
) {
}