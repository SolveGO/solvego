package com.kdh.solvego.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kdh.solvego.domain.attempt.service.AttemptService;
import com.kdh.solvego.domain.problem.dto.WrongProblemResponse;
import com.kdh.solvego.domain.user.dto.SignupRequest;
import com.kdh.solvego.domain.user.dto.SignupResponse;
import com.kdh.solvego.domain.user.exception.DuplicateUsernameException;
import com.kdh.solvego.domain.user.service.UserService;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AttemptService attemptService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("회원가입에 성공하면 201 Created와 userId를 반환한다")
    void signup_success() throws Exception {
        // given
        SignupRequest request = new SignupRequest("username", "1234");

        when(userService.signup(any(SignupRequest.class)))
                .thenReturn(new SignupResponse(1L));

        // when & then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value(1L));

        verify(userService).signup(request);
    }

    @Test
    @DisplayName("회원가입 요청값이 올바르지 않으면 400 Bad Request를 반환한다")
    void signup_fails_when_request_is_invalid() throws Exception {
        // given
        SignupRequest request = new SignupRequest("", "1234");

        // when & then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).signup(any(SignupRequest.class));
    }

    @Test
    @DisplayName("중복된 username이면 409 Conflict를 반환한다")
    void signup_fails_when_username_is_duplicated() throws Exception {
        // given
        SignupRequest request = new SignupRequest("username", "1234");

        when(userService.signup(any(SignupRequest.class)))
                .thenThrow(new DuplicateUsernameException());

        // when & then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());

        verify(userService).signup(request);
    }

    @Test
    @DisplayName("틀린 문제 목록 조회에 성공한다")
    void get_wrong_problems_success() throws Exception {
        // given
        Long userId = 1L;

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(userId, null, List.of());

        List<WrongProblemResponse> response = List.of(
                new WrongProblemResponse(1L, "problem title")
        );

        when(attemptService.getWrongProblems(userId))
                .thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/users/me/wrong-problems")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].problemId").value(1L))
                .andExpect(jsonPath("$[0].title").value("problem title"));

        verify(attemptService).getWrongProblems(userId);
    }
}