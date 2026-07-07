package com.kdh.solvego.domain.attempt.service;

import com.kdh.solvego.domain.attempt.dto.AttemptCreateRequest;
import com.kdh.solvego.domain.attempt.dto.AttemptCreateResponse;
import com.kdh.solvego.domain.attempt.entity.Attempt;
import com.kdh.solvego.domain.attempt.repository.AttemptRepository;
import com.kdh.solvego.domain.common.vo.Position;
import com.kdh.solvego.domain.problem.dto.WrongProblemResponse;
import com.kdh.solvego.domain.problem.entity.PlayerColor;
import com.kdh.solvego.domain.problem.entity.Problem;
import com.kdh.solvego.domain.problem.exception.ProblemNotFoundException;
import com.kdh.solvego.domain.problem.mapper.ProblemMapper;
import com.kdh.solvego.domain.problem.repository.ProblemRepository;
import com.kdh.solvego.domain.user.entity.User;
import com.kdh.solvego.domain.user.exception.UserNotFoundException;
import com.kdh.solvego.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttemptServiceTest {

    @Mock
    private AttemptRepository attemptRepository;

    @Mock
    private ProblemRepository problemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProblemMapper problemMapper;

    @InjectMocks
    private AttemptService attemptService;

    @Test
    @DisplayName("정답 좌표를 제출하면 정답으로 저장한다")
    void create_attempt_success_when_answer_is_correct() {
        // given
        Long problemId = 1L;
        Long userId = 1L;

        User user = createUser(userId, "user");
        Problem problem = createProblem(problemId, user, "problem");

        AttemptCreateRequest request = new AttemptCreateRequest(
                new Position(10, 10)
        );

        when(problemRepository.findById(problemId))
                .thenReturn(Optional.of(problem));

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        // when
        AttemptCreateResponse response = attemptService.createAttempt(
                problemId,
                userId,
                request
        );

        // then
        assertThat(response.isCorrect()).isTrue();

        ArgumentCaptor<Attempt> attemptCaptor =
                ArgumentCaptor.forClass(Attempt.class);

        verify(attemptRepository).save(attemptCaptor.capture());

        Attempt savedAttempt = attemptCaptor.getValue();

        assertThat(savedAttempt.getUser()).isEqualTo(user);
        assertThat(savedAttempt.getProblem()).isEqualTo(problem);
        assertThat(savedAttempt.getSelectedPosition()).isEqualTo(new Position(10, 10));
        assertThat(savedAttempt.isCorrect()).isTrue();

        verify(problemRepository).findById(problemId);
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("오답 좌표를 제출하면 오답으로 저장한다")
    void create_attempt_success_when_answer_is_wrong() {
        // given
        Long problemId = 1L;
        Long userId = 1L;

        User user = createUser(userId, "user");
        Problem problem = createProblem(problemId, user, "problem");

        AttemptCreateRequest request = new AttemptCreateRequest(
                new Position(1, 1)
        );

        when(problemRepository.findById(problemId))
                .thenReturn(Optional.of(problem));

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        // when
        AttemptCreateResponse response = attemptService.createAttempt(
                problemId,
                userId,
                request
        );

        // then
        assertThat(response.isCorrect()).isFalse();

        ArgumentCaptor<Attempt> attemptCaptor =
                ArgumentCaptor.forClass(Attempt.class);

        verify(attemptRepository).save(attemptCaptor.capture());

        Attempt savedAttempt = attemptCaptor.getValue();

        assertThat(savedAttempt.getUser()).isEqualTo(user);
        assertThat(savedAttempt.getProblem()).isEqualTo(problem);
        assertThat(savedAttempt.getSelectedPosition()).isEqualTo(new Position(1, 1));
        assertThat(savedAttempt.isCorrect()).isFalse();

        verify(problemRepository).findById(problemId);
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("존재하지 않는 problemId이면 예외가 발생한다")
    void create_attempt_fails_when_problem_not_found() {
        // given
        Long problemId = 999L;
        Long userId = 1L;

        AttemptCreateRequest request = new AttemptCreateRequest(
                new Position(10, 10)
        );

        when(problemRepository.findById(problemId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> attemptService.createAttempt(
                problemId,
                userId,
                request
        )).isInstanceOf(ProblemNotFoundException.class);

        verify(problemRepository).findById(problemId);
        verifyNoInteractions(userRepository);
        verifyNoInteractions(attemptRepository);
        verifyNoInteractions(problemMapper);
    }

    @Test
    @DisplayName("존재하지 않는 userId이면 예외가 발생한다")
    void create_attempt_fails_when_user_not_found() {
        // given
        Long problemId = 1L;
        Long userId = 999L;

        User creator = createUser(1L, "creator");
        Problem problem = createProblem(problemId, creator, "problem");

        AttemptCreateRequest request = new AttemptCreateRequest(
                new Position(10, 10)
        );

        when(problemRepository.findById(problemId))
                .thenReturn(Optional.of(problem));

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> attemptService.createAttempt(
                problemId,
                userId,
                request
        )).isInstanceOf(UserNotFoundException.class);

        verify(problemRepository).findById(problemId);
        verify(userRepository).findById(userId);
        verifyNoInteractions(attemptRepository);
        verifyNoInteractions(problemMapper);
    }

    @Test
    @DisplayName("틀린 문제 목록을 조회한다")
    void get_wrong_problems_success() {
        // given
        Long userId = 1L;

        User user = createUser(userId, "user");

        Problem problem = createProblem(1L, user, "problem");
        List<Problem> wrongProblems = List.of(problem);

        List<WrongProblemResponse> expectedResponse = List.of(
                new WrongProblemResponse(1L, "problem")
        );

        when(attemptRepository.findWrongProblemsByUserId(userId))
                .thenReturn(wrongProblems);

        when(problemMapper.toWrongProblemResponses(wrongProblems))
                .thenReturn(expectedResponse);

        // when
        List<WrongProblemResponse> response =
                attemptService.getWrongProblems(userId);

        // then
        assertThat(response).isEqualTo(expectedResponse);

        verify(attemptRepository).findWrongProblemsByUserId(userId);
        verify(problemMapper).toWrongProblemResponses(wrongProblems);
    }

    private User createUser(Long userId, String username) {
        User user = new User(username, "encoded-password");
        ReflectionTestUtils.setField(user, "id", userId);
        return user;
    }

    private Problem createProblem(Long problemId, User creator, String title) {
        Problem problem = new Problem(
                title,
                "description",
                List.of(new Position(3, 3)),
                List.of(new Position(4, 4)),
                PlayerColor.BLACK,
                new Position(10, 10),
                creator
        );

        ReflectionTestUtils.setField(problem, "id", problemId);

        return problem;
    }
}