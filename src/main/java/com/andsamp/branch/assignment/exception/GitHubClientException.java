package com.andsamp.branch.assignment.exception;

import org.springframework.http.HttpStatusCode;

public class GitHubClientException extends RuntimeException {
    private HttpStatusCode statusCode;
    private String body;

    public GitHubClientException(String message) {
        super(message);
    }

    public GitHubClientException(String message, HttpStatusCode statusCode, String body) {
        super(message);
        this.statusCode = statusCode;
        this.body = body;
    }

    public HttpStatusCode getStatusCode() {
        return statusCode;
    }
}
