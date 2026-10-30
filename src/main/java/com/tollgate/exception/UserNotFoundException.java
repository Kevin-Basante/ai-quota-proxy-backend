package com.tollgate.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String userId) {
        super("User '" + userId + "' does not exist");
    }
}
