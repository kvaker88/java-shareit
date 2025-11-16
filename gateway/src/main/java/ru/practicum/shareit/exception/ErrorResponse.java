package ru.practicum.shareit.exception;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record ErrorResponse(
        String error,
        String message,
        String reason,
        String timestamp
) {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ErrorResponse(String error, String message, String reason) {
        this(error, message, reason, LocalDateTime.now().format(FORMATTER));
    }
}