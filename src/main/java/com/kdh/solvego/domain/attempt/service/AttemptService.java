package com.kdh.solvego.domain.attempt.service;

import com.kdh.solvego.domain.problem.dto.WrongProblemResponse;
import com.kdh.solvego.domain.attempt.repository.AttemptRepository;
import com.kdh.solvego.domain.attempt.dto.AttemptCreateRequest;
import com.kdh.solvego.domain.attempt.dto.AttemptCreateResponse;
import com.kdh.solvego.domain.attempt.entity.Attempt;
import com.kdh.solvego.domain.problem.entity.Problem;
import com.kdh.solvego.domain.problem.exception.ProblemNotFoundException;
import com.kdh.solvego.domain.problem.mapper.ProblemMapper;
import com.kdh.solvego.domain.problem.repository.ProblemRepository;
import com.kdh.solvego.domain.user.entity.User;
import com.kdh.solvego.domain.user.repository.UserRepository;
import com.kdh.solvego.domain.user.exception.UserNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;


@Service
public class AttemptService {

    private final AttemptRepository attemptRepository;
    private final ProblemRepository problemRepository;
    private final UserRepository userRepository;
    private final ProblemMapper problemMapper;

    public AttemptService(
            AttemptRepository attemptRepository,
            ProblemRepository problemRepository,
            UserRepository userRepository,
            ProblemMapper problemMapper){
        this.attemptRepository = attemptRepository;
        this.problemRepository = problemRepository;
        this.userRepository = userRepository;
        this.problemMapper = problemMapper;
    }

    @Transactional
    public AttemptCreateResponse createAttempt(
            Long problemId,
            Long userId,
            AttemptCreateRequest request
    ){
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(ProblemNotFoundException::new);

        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        boolean isCorrect = problem.isCorrectPosition(request.selectedPosition());

        Attempt attempt = new Attempt(
                user,
                problem,
                request.selectedPosition(),
                isCorrect
        );

        attemptRepository.save(attempt);

        return new AttemptCreateResponse(isCorrect);
    }

    @Transactional(readOnly = true)
    public List<WrongProblemResponse> getWrongProblems(Long userId) {
        List<Problem> wrongProblems =
                attemptRepository.findWrongProblemsByUserId(userId);

        return problemMapper.toWrongProblemResponses(wrongProblems);
    }
}
