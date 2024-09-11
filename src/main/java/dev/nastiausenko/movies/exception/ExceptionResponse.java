package dev.nastiausenko.movies.exception;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public record ExceptionResponse(String message, int status, String path, LocalDateTime timestamp) {
}
