package dev.nastiausenko.movies.exception;

import java.time.LocalDateTime;

public record ExceptionResponse(String message, int status, String path, LocalDateTime timestamp) {
}
