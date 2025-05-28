package com.andsamp.branch.assignment.controller;

import com.andsamp.branch.assignment.exception.GitHubClientException;
import com.andsamp.branch.assignment.exception.GitHubEntityNotFoundException;
import com.andsamp.branch.assignment.exception.RateLimitExceededException;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class ErrorHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {ConstraintViolationException.class})
    protected ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException e, WebRequest request) {

        HttpStatus status = HttpStatus.BAD_REQUEST;
        Map<String, Object> errorBody = generateErrorBody(e.getMessage(), status, request);

        return handleExceptionInternal(e, errorBody, new HttpHeaders(), status, request);
    }

    @ExceptionHandler(value = {GitHubEntityNotFoundException.class})
    protected ResponseEntity<Object> handleGitHubEntityNotFound(GitHubEntityNotFoundException e, WebRequest request) {

        HttpStatus status = HttpStatus.NOT_FOUND;
        Map<String, Object> errorBody = generateErrorBody(e.getMessage(), status, request);

        return handleExceptionInternal(e, errorBody, new HttpHeaders(), status, request);
    }

    @ExceptionHandler(value = {GitHubClientException.class})
    protected ResponseEntity<Object> handleGitHubClientException(GitHubClientException e, WebRequest request) {

        HttpStatus status = (HttpStatus) e.getStatusCode();
        Map<String, Object> errorBody = generateErrorBody(e.getMessage(), status, request);

        return handleExceptionInternal(e, errorBody, new HttpHeaders(), status, request);
    }

    @ExceptionHandler(value = {RateLimitExceededException.class})
    protected ResponseEntity<Object> handleRateLimitExceededException(
            RateLimitExceededException e, WebRequest request) {

        HttpStatus status = HttpStatus.TOO_MANY_REQUESTS;
        Map<String, Object> errorBody = generateErrorBody(e.getMessage(), status, request);

        return handleExceptionInternal(e, errorBody, new HttpHeaders(), status, request);
    }

    private Map<String, Object> generateErrorBody(String message, HttpStatus httpStatus, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now());
        body.put("status", httpStatus.value());
        body.put("message", message);
        body.put("path", request.getDescription(false));

        return body;
    }
}
