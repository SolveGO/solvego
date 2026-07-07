package com.kdh.solvego.domain.problem.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "문제 목록 조회 응답")
public record ProblemListResponse(

        @Schema(description = "문제 목록")
        List<ProblemSummaryResponse> problems
) {
}