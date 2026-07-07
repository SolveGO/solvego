package com.kdh.solvego.domain.problem.mapper;

import com.kdh.solvego.domain.problem.dto.ProblemCreateRequest;
import com.kdh.solvego.domain.problem.dto.ProblemDetailResponse;
import com.kdh.solvego.domain.problem.dto.ProblemListResponse;
import com.kdh.solvego.domain.problem.dto.ProblemSummaryResponse;
import com.kdh.solvego.domain.problem.dto.WrongProblemResponse;
import com.kdh.solvego.domain.problem.entity.Problem;
import com.kdh.solvego.domain.user.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProblemMapper {

    public Problem toEntity(ProblemCreateRequest request, User creator) {
        return new Problem(
                request.title(),
                request.description(),
                request.blackStones(),
                request.whiteStones(),
                request.nextPlayer(),
                request.answerPosition(),
                creator
        );
    }

    public ProblemListResponse toListResponse(List<Problem> problems) {
        List<ProblemSummaryResponse> problemResponses = problems.stream()
                .map(this::toSummaryResponse)
                .toList();

        return new ProblemListResponse(problemResponses);
    }

    public ProblemSummaryResponse toSummaryResponse(Problem problem) {
        return new ProblemSummaryResponse(
                problem.getId(),
                problem.getTitle(),
                problem.getCreator().getUsername()
        );
    }

    public ProblemDetailResponse toDetailResponse(Problem problem) {
        return new ProblemDetailResponse(
                problem.getId(),
                problem.getTitle(),
                problem.getDescription(),
                problem.getBlackStones(),
                problem.getWhiteStones(),
                problem.getNextPlayer(),
                problem.getCreator().getUsername()
        );
    }

    public List<WrongProblemResponse> toWrongProblemResponses(List<Problem> problems) {
        return problems.stream()
                .map(this::toWrongProblemResponse)
                .toList();
    }

    private WrongProblemResponse toWrongProblemResponse(Problem problem) {
        return new WrongProblemResponse(
                problem.getId(),
                problem.getTitle()
        );
    }
}