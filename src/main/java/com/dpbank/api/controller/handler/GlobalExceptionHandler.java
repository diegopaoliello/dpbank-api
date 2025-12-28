package com.dpbank.api.controller.handler;

import com.dpbank.api.service.exception.AccountNotFoundException;
import com.dpbank.api.service.exception.InsufficientBalanceException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Centralizes API error handling so every requirement receives a consistent
 * {@link ProblemDetail} payload already localized via {@link MessageSource}.
 */
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    /**
     * Maps insufficient balance attempts to HTTP 422 with localized details.
     */
    @ExceptionHandler(InsufficientBalanceException.class)
    public ProblemDetail handleSaldoInsuficiente(InsufficientBalanceException ex) {
        ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
        detail.setTitle(message("error.insufficientBalance.title"));
        detail.setDetail(message(ex.getMessageKey(), ex.getMessageArgs()));
        detail.setProperty("timestamp", Instant.now());
        return detail;
    }

    /**
     * Returns HTTP 404 when the requested account does not exist.
     */
    @ExceptionHandler(AccountNotFoundException.class)
    public ProblemDetail handleNotFound(AccountNotFoundException ex) {
        ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        detail.setTitle(message("error.resourceNotFound.title"));
        detail.setDetail(message(ex.getMessageKey(), ex.getMessageArgs()));
        detail.setProperty("timestamp", Instant.now());
        return detail;
    }

    /**
     * Collects bean validation errors and returns them as a structured map.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        detail.setTitle(message("error.validation.title"));
        detail.setDetail(message("error.validation.detail"));
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (a, b) -> a, LinkedHashMap::new));
        detail.setProperty("errors", errors);
        detail.setProperty("timestamp", Instant.now());
        return detail;
    }

    /**
     * Handles basic illegal argument failures such as null IDs.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        detail.setTitle(message("error.invalidRequest.title"));
        detail.setDetail(message(ex.getMessage()));
        detail.setProperty("timestamp", Instant.now());
        return detail;
    }

    /**
     * Converts constraint violations triggered on path/query parameters to a
     * friendly response body.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex) {
        ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        detail.setTitle(message("error.validation.title"));
        detail.setDetail(message("error.validation.detail"));
        Map<String, String> errors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(v -> v.getPropertyPath().toString(), ConstraintViolation::getMessage,
                        (a, b) -> a, LinkedHashMap::new));
        detail.setProperty("errors", errors);
        detail.setProperty("timestamp", Instant.now());
        return detail;
    }

    /**
     * Resolves the given message key using the current request locale.
     */
    private String message(String key, Object... args) {
        return messageSource.getMessage(key, args, key, LocaleContextHolder.getLocale());
    }
}
