package com.kdh.solvego.domain.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "바둑판 좌표")
public record Position(

        @Schema(description = "x 좌표", example = "3")
        @NotNull
        @Min(value = 0)
        @Max(value = 18)
        Integer x,

        @Schema(description = "y 좌표", example = "4")
        @NotNull
        @Min(value = 0)
        @Max(value = 18)
        Integer y
) {
}