package com.kdh.solvego.domain.attempt.controller;

import com.kdh.solvego.domain.attempt.dto.AttemptCreateRequest;
import com.kdh.solvego.domain.attempt.dto.AttemptCreateResponse;
import com.kdh.solvego.domain.attempt.service.AttemptService;
import com.kdh.solvego.global.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Attempt", description = "풀이 시도 관련 API")
@RestController
@RequestMapping(
        value="/api/problems/{problemId}/attempts",
        produces = MediaType.APPLICATION_JSON_VALUE
)
public class AttemptController {

    private final AttemptService attemptService;

    public AttemptController(AttemptService attemptService) {
        this.attemptService = attemptService;
    }

    @Operation(
            summary = "문제 풀이 제출",
            description = "사용자가 선택한 착수 좌표를 제출하고 정답 여부를 판별한 뒤 풀이 기록을 저장합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "풀이 시도 저장 및 정답 여부 판별 완료"),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 형식 오류",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 문제",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })

    @SecurityRequirement(name = "bearerAuth")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public AttemptCreateResponse createAttempt(
            @PathVariable Long problemId,
            Authentication authentication,
            @Valid @RequestBody AttemptCreateRequest request
    ) {
        Long userId = (Long) authentication.getPrincipal();

        AttemptCreateResponse response = attemptService.createAttempt(
                problemId,
                userId,
                request
        );

        return response;
    }
}
