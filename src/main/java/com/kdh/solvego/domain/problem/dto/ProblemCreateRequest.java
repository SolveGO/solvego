package com.kdh.solvego.domain.problem.dto;

import com.kdh.solvego.domain.common.vo.Position;
import com.kdh.solvego.domain.problem.entity.PlayerColor;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "문제 등록 요청")
public record ProblemCreateRequest(

        @Schema(description = "문제 제목", example = "화점 사활 문제")
        @NotBlank
        @Size(max = 50)
        String title,

        @Schema(description = "문제 설명", example = "흑이 살 수 있는 수를 찾으세요.")
        @NotBlank
        @Size(max = 100)
        String description,

        @Schema(description = "흑돌 좌표 목록")
        @NotEmpty
        List<@Valid Position> blackStones,

        @Schema(description = "백돌 좌표 목록")
        @NotEmpty
        List<@Valid Position> whiteStones,

        @Schema(description = "다음 착수자", example = "BLACK")
        @NotNull
        PlayerColor nextPlayer,

        @Schema(description = "정답 좌표")
        @NotNull
        @Valid
        Position answerPosition

) {
}