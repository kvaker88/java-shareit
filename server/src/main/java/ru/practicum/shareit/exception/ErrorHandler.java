package ru.practicum.shareit.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleUserNotFoundException(final NotFoundException e) {
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleAccessDeniedException(final AccessDeniedException e) {
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler()
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleEmailAlreadyExistsException(final EmailAlreadyExistsException e) {
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ValidationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(final Exception e) {
        String errorMessage;

        if (e instanceof MethodArgumentNotValidException ex) {
            errorMessage = ex.getBindingResult().getFieldErrors().stream()
                    .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                    .findFirst()
                    .orElse("Не удалось выполнить проверку");
        } else {
            errorMessage = e.getMessage();
        }

        return new ErrorResponse(errorMessage);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgumentException(final IllegalArgumentException e) {
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMissingRequestHeaderException(final MissingRequestHeaderException e) {
        return new ErrorResponse(String.format("Обязательный заголовок '%s' отсутствует", e.getHeaderName()));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNoSuchElement(NoSuchElementException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleConstraintViolationException(final ConstraintViolationException e) {
        String errorMessage = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .findFirst()
                .orElse("Не удалось выполнить проверку");
        return new ErrorResponse(errorMessage);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(HandlerMethodValidationException e) {
        String errorMessage = e.getAllValidationResults().stream()
                .map(result -> result.getResolvableErrors().stream()
                        .map(MessageSourceResolvable::getDefaultMessage)
                        .collect(Collectors.joining(", ")))
                .collect(Collectors.joining("; "));

        return ResponseEntity.badRequest()
                .body(new ErrorResponse(errorMessage));
    }
}