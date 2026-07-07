package com.kdh.solvego.domain.problem.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "틀린 문제 항목 응답")
public record WrongProblemResponse(

        @Schema(description = "문제 ID", example = "1")
        Long problemId,

        @Schema(description = "문제 제목", example = "화점 사활 문제")
        String title
) {
}