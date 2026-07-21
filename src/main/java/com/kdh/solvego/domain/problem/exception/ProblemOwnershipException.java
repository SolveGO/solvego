package com.kdh.solvego.domain.problem.exception;

public class ProblemOwnershipException extends RuntimeException {

    public ProblemOwnershipException() {
        super("You do not have permission to modify this problem");
    }
}