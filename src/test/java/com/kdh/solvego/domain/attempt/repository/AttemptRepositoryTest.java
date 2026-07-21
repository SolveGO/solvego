package com.kdh.solvego.domain.attempt.repository;

import com.kdh.solvego.domain.attempt.entity.Attempt;
import com.kdh.solvego.domain.common.vo.Position;
import com.kdh.solvego.domain.problem.entity.PlayerColor;
import com.kdh.solvego.domain.problem.entity.Problem;
import com.kdh.solvego.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AttemptRepositoryTest {

    @Autowired
    private AttemptRepository attemptRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("오답 attempt가 있으면 해당 문제를 반환한다")
    void find_wrong_problems_returns_wrong_problem() {
        // given
        User user = persistUser("user");

        Problem problem = persistProblem(user, "problem");

        persistAttempt(user, problem, false, LocalDateTime.of(2026, 1, 1, 0, 0));

        entityManager.clear();

        // when
        List<Problem> wrongProblems = attemptRepository.findWrongProblemsByUserId(user.getId());

        // then
        assertThat(wrongProblems).hasSize(1);
        assertThat(wrongProblems.get(0).getTitle()).isEqualTo("problem");
    }

    @Test
    @DisplayName("정답 attempt만 있으면 빈 리스트를 반환한다")
    void find_wrong_problems_returns_empty_when_only_correct_attempts_exist() {
        // given
        User user = persistUser("user");

        Problem problem = persistProblem(user, "problem");

        persistAttempt(user, problem, true, LocalDateTime.of(2026, 1, 1, 0, 0));

        entityManager.clear();

        // when
        List<Problem> wrongProblems = attemptRepository.findWrongProblemsByUserId(user.getId());

        // then
        assertThat(wrongProblems).isEmpty();
    }

    @Test
    @DisplayName("같은 문제를 여러 번 틀려도 한 번만 반환한다")
    void find_wrong_problems_removes_duplicate_problem() {
        // given
        User user = persistUser("user");

        Problem problem = persistProblem(user, "problem");

        persistAttempt(user, problem, false, LocalDateTime.of(2026, 1, 1, 0, 0));
        persistAttempt(user, problem, false, LocalDateTime.of(2026, 1, 2, 0, 0));
        persistAttempt(user, problem, false, LocalDateTime.of(2026, 1, 3, 0, 0));

        entityManager.clear();

        // when
        List<Problem> wrongProblems = attemptRepository.findWrongProblemsByUserId(user.getId());

        // then
        assertThat(wrongProblems).hasSize(1);
        assertThat(wrongProblems.get(0).getTitle()).isEqualTo("problem");
    }

    @Test
    @DisplayName("오답 문제를 최근에 틀린 순서로 반환한다")
    void find_wrong_problems_order_by_latest_wrong_attempt() {
        // given
        User user = persistUser("user");

        Problem oldProblem = persistProblem(user, "old problem");
        Problem newProblem = persistProblem(user, "new problem");

        persistAttempt(user, oldProblem, false, LocalDateTime.of(2026, 1, 1, 0, 0));
        persistAttempt(user, newProblem, false, LocalDateTime.of(2026, 1, 3, 0, 0));

        entityManager.clear();

        // when
        List<Problem> wrongProblems = attemptRepository.findWrongProblemsByUserId(user.getId());

        // then
        assertThat(wrongProblems).hasSize(2);
        assertThat(wrongProblems)
                .extracting(Problem::getTitle)
                .containsExactly("new problem", "old problem");
    }

    @Test
    @DisplayName("다른 사용자의 오답 문제는 반환하지 않는다")
    void find_wrong_problems_excludes_other_users_wrong_problems() {
        // given
        User user = persistUser("user");
        User otherUser = persistUser("otherUser");

        Problem userProblem = persistProblem(user, "user problem");
        Problem otherUserProblem = persistProblem(otherUser, "other user problem");

        persistAttempt(user, userProblem, false, LocalDateTime.of(2026, 1, 1, 0, 0));
        persistAttempt(otherUser, otherUserProblem, false, LocalDateTime.of(2026, 1, 2, 0, 0));

        entityManager.clear();

        // when
        List<Problem> wrongProblems = attemptRepository.findWrongProblemsByUserId(user.getId());

        // then
        assertThat(wrongProblems).hasSize(1);
        assertThat(wrongProblems.get(0).getTitle()).isEqualTo("user problem");
    }

    @Test
    @DisplayName("problemId에 해당하는 모든 attempt를 삭제한다")
    void delete_all_by_problem_id() {
        // given
        User user = persistUser("user");

        Problem targetProblem = persistProblem(user, "target problem");
        Problem otherProblem = persistProblem(user, "other problem");

        persistAttempt(
                user,
                targetProblem,
                false,
                LocalDateTime.of(2026, 1, 1, 0, 0)
        );

        persistAttempt(
                user,
                targetProblem,
                true,
                LocalDateTime.of(2026, 1, 2, 0, 0)
        );

        persistAttempt(
                user,
                otherProblem,
                false,
                LocalDateTime.of(2026, 1, 3, 0, 0)
        );

        entityManager.clear();

        // when
        attemptRepository.deleteAllByProblemId(targetProblem.getId());
        entityManager.flush();
        entityManager.clear();

        // then
        List<Attempt> attempts = attemptRepository.findAll();

        assertThat(attempts).hasSize(1);
        assertThat(attempts.get(0).getProblem().getId())
                .isEqualTo(otherProblem.getId());
    }

    private User persistUser(String username) {
        User user = new User(username, "encoded-password");
        entityManager.persist(user);
        return user;
    }

    private Problem persistProblem(User creator, String title) {
        Problem problem = new Problem(
                title,
                "description",
                List.of(new Position(3, 3)),
                List.of(new Position(4, 4)),
                PlayerColor.BLACK,
                new Position(10, 10),
                creator
        );

        entityManager.persist(problem);
        return problem;
    }

    private Attempt persistAttempt(
            User user,
            Problem problem,
            boolean isCorrect,
            LocalDateTime attemptedAt
    ) {
        Attempt attempt = new Attempt(user, problem, new Position(1, 1), isCorrect);
        entityManager.persist(attempt);
        entityManager.flush();

        jdbcTemplate.update(
                "update attempts set attempted_at = ? where id = ?",
                Timestamp.valueOf(attemptedAt),
                attempt.getId()
        );

        return attempt;
    }
}