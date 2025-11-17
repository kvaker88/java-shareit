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
    private static final String VALIDATION_ERROR = "Validation error";
    private static final String NOT_FOUND_ERROR = "Not Found";
    private static final String BAD_REQUEST_ERROR = "Bad Request";
    private static final String INTERNAL_SERVER_ERROR = "Internal Server Error";
    private static final String FORBIDDEN_ERROR = "Forbidden";
    private static final String CONFLICT_ERROR = "Conflict";

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleUserNotFoundException(final NotFoundException e) {
        return new ErrorResponse(NOT_FOUND_ERROR, e.getMessage(), "Запрошенный ресурс не найден");
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleAccessDeniedException(final AccessDeniedException e) {
        return new ErrorResponse(FORBIDDEN_ERROR, e.getMessage(), "Ошибка прав доступа");
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleEmailAlreadyExistsException(final EmailAlreadyExistsException e) {
        return new ErrorResponse(CONFLICT_ERROR, e.getMessage(), "Электронная почта уже существует");
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

        return new ErrorResponse(VALIDATION_ERROR, errorMessage, "Ошибка валидации запроса");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgumentException(final IllegalArgumentException e) {
        return new ErrorResponse(BAD_REQUEST_ERROR, e.getMessage(), "Недопустимые параметры запроса");
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMissingRequestHeaderException(final MissingRequestHeaderException e) {
        String message = String.format("Обязательный заголовок '%s' отсутствует", e.getHeaderName());
        return new ErrorResponse(BAD_REQUEST_ERROR, message, "Отсутствует обязательный заголовок");
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNoSuchElement(NoSuchElementException e) {
        ErrorResponse errorResponse = new ErrorResponse(NOT_FOUND_ERROR, e.getMessage(), "Запрошенный ресурс не найден");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleConstraintViolationException(final ConstraintViolationException e) {
        String errorMessage = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .findFirst()
                .orElse("Не удалось выполнить проверку");
        return new ErrorResponse(VALIDATION_ERROR, errorMessage, "Нарушение ограничений");
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(HandlerMethodValidationException e) {
        String errorMessage = e.getAllValidationResults().stream()
                .map(result -> result.getResolvableErrors().stream()
                        .map(MessageSourceResolvable::getDefaultMessage)
                        .collect(Collectors.joining(", ")))
                .collect(Collectors.joining("; "));

        ErrorResponse errorResponse = new ErrorResponse(VALIDATION_ERROR, errorMessage, "Не удалось выполнить проверку метода");
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleException(final Exception e) {
        return new ErrorResponse(INTERNAL_SERVER_ERROR, "Произошла непредвиденная ошибка", "Ошибка сервера");
    }

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleThrowable(final Throwable t) {
        return new ErrorResponse(INTERNAL_SERVER_ERROR, "Произошла критическая ошибка", "Внутренняя ошибка сервера");
    }
}