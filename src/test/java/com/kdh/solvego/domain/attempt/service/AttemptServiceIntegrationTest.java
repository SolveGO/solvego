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
import com.kdh.solvego.domain.problem.repository.ProblemRepository;
import com.kdh.solvego.domain.user.entity.User;
import com.kdh.solvego.domain.user.exception.UserNotFoundException;
import com.kdh.solvego.domain.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AttemptServiceIntegrationTest {

    @Autowired
    private AttemptService attemptService;

    @Autowired
    private AttemptRepository attemptRepository;

    @Autowired
    private ProblemRepository problemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("정답 좌표를 제출하면 정답으로 Attempt가 저장된다")
    void create_attempt_success_when_answer_is_correct() {
        // given
        User user = userRepository.save(new User("user", "encoded-password"));

        Problem problem = problemRepository.save(new Problem(
                "problem",
                "description",
                List.of(new Position(3, 3)),
                List.of(new Position(4, 4)),
                PlayerColor.BLACK,
                new Position(10, 10),
                user
        ));

        AttemptCreateRequest request = new AttemptCreateRequest(
                new Position(10, 10)
        );

        // when
        AttemptCreateResponse response = attemptService.createAttempt(
                problem.getId(),
                user.getId(),
                request
        );

        entityManager.flush();
        entityManager.clear();

        // then
        assertThat(response.isCorrect()).isTrue();

        List<Attempt> attempts = attemptRepository.findAll();

        assertThat(attempts).hasSize(1);

        Attempt savedAttempt = attempts.get(0);

        assertThat(savedAttempt.getUser().getId()).isEqualTo(user.getId());
        assertThat(savedAttempt.getProblem().getId()).isEqualTo(problem.getId());
        assertThat(savedAttempt.getSelectedPosition()).isEqualTo(new Position(10, 10));
        assertThat(savedAttempt.isCorrect()).isTrue();
    }

    @Test
    @DisplayName("오답 좌표를 제출하면 오답으로 Attempt가 저장된다")
    void create_attempt_success_when_answer_is_wrong() {
        // given
        User user = userRepository.save(new User("user", "encoded-password"));

        Problem problem = problemRepository.save(new Problem(
                "problem",
                "description",
                List.of(new Position(3, 3)),
                List.of(new Position(4, 4)),
                PlayerColor.BLACK,
                new Position(10, 10),
                user
        ));

        AttemptCreateRequest request = new AttemptCreateRequest(
                new Position(1, 1)
        );

        // when
        AttemptCreateResponse response = attemptService.createAttempt(
                problem.getId(),
                user.getId(),
                request
        );

        entityManager.flush();
        entityManager.clear();

        // then
        assertThat(response.isCorrect()).isFalse();

        List<Attempt> attempts = attemptRepository.findAll();

        assertThat(attempts).hasSize(1);

        Attempt savedAttempt = attempts.get(0);

        assertThat(savedAttempt.getUser().getId()).isEqualTo(user.getId());
        assertThat(savedAttempt.getProblem().getId()).isEqualTo(problem.getId());
        assertThat(savedAttempt.getSelectedPosition()).isEqualTo(new Position(1, 1));
        assertThat(savedAttempt.isCorrect()).isFalse();
    }

    @Test
    @DisplayName("존재하지 않는 problemId이면 예외가 발생한다")
    void create_attempt_fails_when_problem_not_found() {
        // given
        User user = userRepository.save(new User("user", "encoded-password"));

        AttemptCreateRequest request = new AttemptCreateRequest(
                new Position(10, 10)
        );

        // when & then
        assertThatThrownBy(() -> attemptService.createAttempt(
                999L,
                user.getId(),
                request
        )).isInstanceOf(ProblemNotFoundException.class);
    }

    @Test
    @DisplayName("존재하지 않는 userId이면 예외가 발생한다")
    void create_attempt_fails_when_user_not_found() {
        // given
        User creator = userRepository.save(new User("creator", "encoded-password"));

        Problem problem = problemRepository.save(new Problem(
                "problem",
                "description",
                List.of(new Position(3, 3)),
                List.of(new Position(4, 4)),
                PlayerColor.BLACK,
                new Position(10, 10),
                creator
        ));

        AttemptCreateRequest request = new AttemptCreateRequest(
                new Position(10, 10)
        );

        // when & then
        assertThatThrownBy(() -> attemptService.createAttempt(
                problem.getId(),
                999L,
                request
        )).isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("틀린 문제 목록을 조회한다")
    void get_wrong_problems_success() {
        // given
        User user = userRepository.save(new User("user", "encoded-password"));

        Problem problem = problemRepository.save(new Problem(
                "problem",
                "description",
                List.of(new Position(3, 3)),
                List.of(new Position(4, 4)),
                PlayerColor.BLACK,
                new Position(10, 10),
                user
        ));

        attemptService.createAttempt(
                problem.getId(),
                user.getId(),
                new AttemptCreateRequest(new Position(1, 1))
        );

        entityManager.flush();
        entityManager.clear();

        // when
        List<WrongProblemResponse> response =
                attemptService.getWrongProblems(user.getId());

        // then
        assertThat(response).hasSize(1);
        assertThat(response.get(0).problemId()).isEqualTo(problem.getId());
        assertThat(response.get(0).title()).isEqualTo("problem");
    }

    @Test
    @DisplayName("틀린 문제 목록은 중복을 제거하고 최신 오답순으로 조회한다")
    void get_wrong_problems_distinct_and_ordered_by_latest_wrong_attempt() throws InterruptedException {
        // given
        User user = userRepository.save(new User("user", "encoded-password"));

        Problem problem1 = problemRepository.save(new Problem(
                "problem1",
                "description",
                List.of(new Position(3, 3)),
                List.of(new Position(4, 4)),
                PlayerColor.BLACK,
                new Position(10, 10),
                user
        ));

        Problem problem2 = problemRepository.save(new Problem(
                "problem2",
                "description",
                List.of(new Position(5, 5)),
                List.of(new Position(6, 6)),
                PlayerColor.WHITE,
                new Position(11, 11),
                user
        ));

        attemptService.createAttempt(
                problem1.getId(),
                user.getId(),
                new AttemptCreateRequest(new Position(1, 1))
        );

        Thread.sleep(10);

        attemptService.createAttempt(
                problem2.getId(),
                user.getId(),
                new AttemptCreateRequest(new Position(2, 2))
        );

        Thread.sleep(10);

        attemptService.createAttempt(
                problem1.getId(),
                user.getId(),
                new AttemptCreateRequest(new Position(3, 3))
        );

        entityManager.flush();
        entityManager.clear();

        // when
        List<WrongProblemResponse> response =
                attemptService.getWrongProblems(user.getId());

        // then
        assertThat(response).hasSize(2);

        assertThat(response.get(0).problemId()).isEqualTo(problem1.getId());
        assertThat(response.get(0).title()).isEqualTo("problem1");

        assertThat(response.get(1).problemId()).isEqualTo(problem2.getId());
        assertThat(response.get(1).title()).isEqualTo("problem2");
    }
}