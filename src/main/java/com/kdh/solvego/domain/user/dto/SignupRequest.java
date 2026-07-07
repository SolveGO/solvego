package com.kdh.solvego.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "회원가입 요청")
public record SignupRequest(

        @Schema(description = "사용자 아이디", example = "solvego123")
        @NotBlank
        @Size(min = 6, max = 20)
        String username,

        @Schema(description = "비밀번호", example = "password1234")
        @NotBlank
        String password
) {
}