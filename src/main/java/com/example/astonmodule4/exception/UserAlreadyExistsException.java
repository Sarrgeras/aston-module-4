package com.example.astonmodule4.exception;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String email) {
        super("User with email already exists: " + email);
    }
}
