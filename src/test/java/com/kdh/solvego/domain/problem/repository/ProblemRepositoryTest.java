package com.kdh.solvego.domain.problem.repository;

import com.kdh.solvego.domain.common.vo.Position;
import com.kdh.solvego.domain.problem.entity.PlayerColor;
import com.kdh.solvego.domain.problem.entity.Problem;
import com.kdh.solvego.domain.user.entity.User;
import jakarta.persistence.PersistenceUnitUtil;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProblemRepositoryTest {

    @Autowired
    private ProblemRepository problemRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("문제 목록을 최신순으로 조회한다")
    void find_all_with_creator_order_by_created_at_desc() {
        // given
        User creator = new User("creator", "encoded-password");
        entityManager.persist(creator);

        Problem oldProblem = createProblem(creator, "old problem");
        Problem newProblem = createProblem(creator, "new problem");

        entityManager.persist(oldProblem);
        entityManager.persist(newProblem);
        entityManager.flush();

        jdbcTemplate.update(
                "update problems set created_at = ? where id = ?",
                Timestamp.valueOf(LocalDateTime.of(2026, 1, 1, 0, 0)),
                oldProblem.getId()
        );

        jdbcTemplate.update(
                "update problems set created_at = ? where id = ?",
                Timestamp.valueOf(LocalDateTime.of(2026, 1, 2, 0, 0)),
                newProblem.getId()
        );

        entityManager.clear();

        // when
        List<Problem> problems = problemRepository.findAllWithCreatorOrderByCreatedAtDesc();

        // then
        assertThat(problems).hasSize(2);
        assertThat(problems)
                .extracting(Problem::getTitle)
                .containsExactly("new problem", "old problem");

        PersistenceUnitUtil persistenceUnitUtil = getPersistenceUnitUtil();

        assertThat(problems)
                .allSatisfy(problem ->
                        assertThat(persistenceUnitUtil.isLoaded(problem.getCreator())).isTrue()
                );
    }

    @Test
    @DisplayName("문제가 없으면 빈 리스트를 반환한다")
    void find_all_with_creator_returns_empty_list() {
        // when
        List<Problem> problems = problemRepository.findAllWithCreatorOrderByCreatedAtDesc();

        // then
        assertThat(problems).isEmpty();
    }

    @Test
    @DisplayName("problemId로 문제와 작성자를 함께 조회한다")
    void find_by_id_with_creator_returns_problem() {
        // given
        User creator = new User("creator", "encoded-password");
        entityManager.persist(creator);

        Problem problem = createProblem(creator, "problem");
        entityManager.persist(problem);
        entityManager.flush();
        entityManager.clear();

        // when
        Optional<Problem> foundProblem = problemRepository.findByIdWithCreator(problem.getId());

        // then
        assertThat(foundProblem).isPresent();
        assertThat(foundProblem.get().getTitle()).isEqualTo("problem");
        assertThat(foundProblem.get().getBlackStones())
                .containsExactly(new Position(3, 3));

        assertThat(foundProblem.get().getWhiteStones())
                .containsExactly(new Position(4, 4));


        PersistenceUnitUtil persistenceUnitUtil = getPersistenceUnitUtil();

        assertThat(persistenceUnitUtil.isLoaded(foundProblem.get().getCreator())).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 problemId이면 Optional.empty를 반환한다")
    void find_by_id_with_creator_returns_empty() {
        // when
        Optional<Problem> foundProblem = problemRepository.findByIdWithCreator(999L);

        // then
        assertThat(foundProblem).isEmpty();
    }

    private Problem createProblem(User creator, String title) {
        return new Problem(
                title,
                "description",
                List.of(new Position(3, 3)),
                List.of(new Position(4, 4)),
                PlayerColor.BLACK,
                new Position(10, 10),
                creator
        );
    }

    private PersistenceUnitUtil getPersistenceUnitUtil() {
        return entityManager.getEntityManager()
                .getEntityManagerFactory()
                .getPersistenceUnitUtil();
    }
}