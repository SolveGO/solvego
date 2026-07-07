package com.kdh.solvego.domain.auth.exception;

public class InvalidLoginException extends RuntimeException {

    public InvalidLoginException() {
        super("Invalid username or password");
    }
}