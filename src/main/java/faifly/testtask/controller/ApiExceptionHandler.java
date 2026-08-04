package faifly.testtask.controller;

import faifly.testtask.exception.ResourceNotFoundException;
import faifly.testtask.exception.VisitOverlapException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.method.MethodValidationException;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleBodyValidation(MethodArgumentNotValidException exception,
                                              HttpServletRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors()
                .forEach(error -> errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        exception.getBindingResult().getGlobalErrors()
                .forEach(error -> errors.putIfAbsent(error.getObjectName(), error.getDefaultMessage()));

        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST,
                "validation-error",
                "Validation failed",
                "The request body failed validation",
                request);
        problem.setProperty("errors", errors);
        return problem;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleParameterValidation(ConstraintViolationException exception,
                                                   HttpServletRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        exception.getConstraintViolations().forEach(violation -> {
            String path = violation.getPropertyPath().toString();
            errors.putIfAbsent(path.substring(path.lastIndexOf('.') + 1), violation.getMessage());
        });

        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST,
                "validation-error",
                "Validation failed",
                "One or more request parameters are invalid",
                request);
        problem.setProperty("errors", errors);
        return problem;
    }

    @ExceptionHandler({HandlerMethodValidationException.class, MethodValidationException.class})
    public ProblemDetail handleMethodValidation(MethodValidationException exception,
                                                HttpServletRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        exception.getParameterValidationResults().forEach(result ->
                result.getResolvableErrors().forEach(error ->
                        errors.putIfAbsent(result.getMethodParameter().getParameterName(), error.getDefaultMessage())));

        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST,
                "validation-error",
                "Validation failed",
                "One or more request parameters are invalid",
                request);
        problem.setProperty("errors", errors);
        return problem;
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ProblemDetail handleMalformedRequest(Exception exception, HttpServletRequest request) {
        String detail;
        if (exception instanceof MethodArgumentTypeMismatchException mismatch) {
            detail = "Parameter '%s' has an invalid value".formatted(mismatch.getName());
        } else {
            detail = "The request body could not be parsed";
        }

        return problem(HttpStatus.BAD_REQUEST,
                "malformed-request",
                "Malformed request",
                detail,
                request);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException exception, HttpServletRequest request) {
        return problem(HttpStatus.NOT_FOUND,
                "resource-not-found",
                "Resource not found",
                exception.getMessage(),
                request);
    }

    @ExceptionHandler(VisitOverlapException.class)
    public ProblemDetail handleOverlap(VisitOverlapException exception, HttpServletRequest request) {
        return problem(HttpStatus.CONFLICT,
                "visit-overlap",
                "Visit overlap",
                exception.getMessage(),
                request);
    }

    @ExceptionHandler(ErrorResponseException.class)
    public ProblemDetail handleErrorResponse(ErrorResponseException exception, HttpServletRequest request) {
        HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
        return problem(status,
                status.is4xxClientError() ? "malformed-request" : "internal-error",
                status.getReasonPhrase(),
                exception.getBody().getDetail(),
                request);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception exception, HttpServletRequest request) {
        return problem(HttpStatus.INTERNAL_SERVER_ERROR,
                "internal-error",
                "Internal server error",
                "The request could not be processed",
                request);
    }

    private ProblemDetail problem(HttpStatus status,
                                  String type,
                                  String title,
                                  String detail,
                                  HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(URI.create("https://faifly.test.task/problems/" + type));
        problem.setTitle(title);
        problem.setInstance(URI.create(request.getRequestURI()));
        return problem;
    }
}
