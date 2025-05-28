package com.andsamp.branch.assignment.exception;

public class GitHubEntityNotFoundException extends RuntimeException {

    public GitHubEntityNotFoundException(String message, Exception cause) {
        super(message, cause);
    }

    public GitHubEntityNotFoundException(String message) {
        super(message);
    }
}
