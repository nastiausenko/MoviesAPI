package dev.nastiausenko.movies.config.exception;

import java.time.LocalDateTime;

public record ExceptionResponse(Object message, int status, String path, LocalDateTime timestamp) {
}
