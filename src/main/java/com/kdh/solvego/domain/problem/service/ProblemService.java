package com.kdh.solvego.domain.problem.service;

import com.kdh.solvego.domain.problem.repository.ProblemRepository;
import com.kdh.solvego.domain.problem.dto.*;
import com.kdh.solvego.domain.problem.entity.Problem;
import com.kdh.solvego.domain.problem.exception.ProblemNotFoundException;
import com.kdh.solvego.domain.problem.mapper.ProblemMapper;
import com.kdh.solvego.domain.user.entity.User;
import com.kdh.solvego.domain.user.repository.UserRepository;
import com.kdh.solvego.domain.user.exception.UserNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProblemService {

    private final ProblemRepository problemRepository;
    private final UserRepository userRepository;
    private final ProblemMapper problemMapper;

    public ProblemService(ProblemRepository problemRepository, UserRepository userRepository, ProblemMapper problemMapper) {
        this.problemRepository = problemRepository;
        this.userRepository = userRepository;
        this.problemMapper = problemMapper;
    }

    @Transactional(readOnly = true)
    public ProblemListResponse getProblems(){
        List<Problem> problems = problemRepository.findAllWithCreatorOrderByIdDesc();
        return problemMapper.toListResponse(problems);
    }

    @Transactional
    public ProblemCreateResponse createProblem(Long userId, ProblemCreateRequest request) {
        User creator=userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        Problem problem = problemMapper.toEntity(request, creator);
        Problem savedProblem = problemRepository.save(problem);
        return new ProblemCreateResponse(savedProblem.getId());
    }

    @Transactional(readOnly = true)
    public ProblemDetailResponse getProblem(Long problemId) {
        Problem problem = problemRepository.findByIdWithCreator(problemId)
                .orElseThrow(ProblemNotFoundException::new);
        return problemMapper.toDetailResponse(problem);
    }

}
