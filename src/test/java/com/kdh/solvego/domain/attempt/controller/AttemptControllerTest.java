package com.kdh.solvego.domain.attempt.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kdh.solvego.domain.attempt.dto.AttemptCreateRequest;
import com.kdh.solvego.domain.attempt.dto.AttemptCreateResponse;
import com.kdh.solvego.domain.attempt.service.AttemptService;
import com.kdh.solvego.domain.common.vo.Position;
import com.kdh.solvego.domain.problem.exception.ProblemNotFoundException;
import com.kdh.solvego.global.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AttemptController.class)
@AutoConfigureMockMvc(addFilters = false)
class AttemptControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AttemptService attemptService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("풀이 제출에 성공하면 201 Created와 정답 여부를 반환한다")
    void create_attempt_success() throws Exception {
        // given
        Long problemId = 1L;
        Long userId = 1L;

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(userId, null, List.of());

        AttemptCreateRequest request =
                new AttemptCreateRequest(new Position(10, 10));

        when(attemptService.createAttempt(
                eq(problemId),
                eq(userId),
                any(AttemptCreateRequest.class)
        )).thenReturn(new AttemptCreateResponse(true));

        // when & then
        mockMvc.perform(post("/api/problems/{problemId}/attempts", problemId)
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isCorrect").value(true));

        verify(attemptService).createAttempt(
                eq(problemId),
                eq(userId),
                any(AttemptCreateRequest.class)
        );
    }

    @Test
    @DisplayName("풀이 제출 요청값이 올바르지 않으면 400 Bad Request를 반환한다")
    void create_attempt_fails_when_request_is_invalid() throws Exception {
        // given
        Long problemId = 1L;
        Long userId = 1L;

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(userId, null, List.of());

        String invalidRequestBody = """
                {
                  "selectedPosition": {
                    "x": -1,
                    "y": 10
                  }
                }
                """;

        // when & then
        mockMvc.perform(post("/api/problems/{problemId}/attempts", problemId)
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestBody))
                .andExpect(status().isBadRequest());

        verify(attemptService, never()).createAttempt(
                any(Long.class),
                any(Long.class),
                any(AttemptCreateRequest.class)
        );
    }

    @Test
    @DisplayName("존재하지 않는 문제에 풀이를 제출하면 404 Not Found를 반환한다")
    void create_attempt_fails_when_problem_not_found() throws Exception {
        // given
        Long problemId = 999L;
        Long userId = 1L;

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(userId, null, List.of());

        AttemptCreateRequest request =
                new AttemptCreateRequest(new Position(10, 10));

        when(attemptService.createAttempt(
                eq(problemId),
                eq(userId),
                any(AttemptCreateRequest.class)
        )).thenThrow(new ProblemNotFoundException());

        // when & then
        mockMvc.perform(post("/api/problems/{problemId}/attempts", problemId)
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(attemptService).createAttempt(
                eq(problemId),
                eq(userId),
                any(AttemptCreateRequest.class)
        );
    }
}