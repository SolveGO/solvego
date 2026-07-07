package com.kdh.solvego.domain.user.exception;

public class DuplicateUsernameException extends RuntimeException {

    public DuplicateUsernameException() {
        super("Username already exists");
    }
}