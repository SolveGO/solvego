package com.kdh.solvego.domain.problem.exception;

public class ProblemNotFoundException extends RuntimeException {

    public ProblemNotFoundException() {
        super("Problem not found");
    }
}