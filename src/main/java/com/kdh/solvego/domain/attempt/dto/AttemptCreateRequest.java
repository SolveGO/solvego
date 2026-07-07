package com.kdh.solvego.domain.attempt.dto;

import com.kdh.solvego.domain.common.vo.Position;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Schema(description = "문제 풀이 제출 요청")
public record AttemptCreateRequest(

        @Schema(description = "사용자가 선택한 착수 좌표")
        @NotNull
        @Valid
        Position selectedPosition
) {
}