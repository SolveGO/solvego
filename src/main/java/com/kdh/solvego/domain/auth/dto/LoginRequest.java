package com.kdh.solvego.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "로그인 요청")
public record LoginRequest (

        @Schema(description = "사용자 아이디", example = "solvego123")
        @NotBlank
        String username,

        @Schema(description = "비밀번호", example = "password1234")
        @NotBlank
        String password
){

}

