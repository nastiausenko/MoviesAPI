package dev.nastiausenko.movies.exception;

import dev.nastiausenko.movies.admin.exception.AdminRightsException;
import dev.nastiausenko.movies.category.exception.CategoryNotFoundException;
import dev.nastiausenko.movies.review.exception.ForbiddenException;
import dev.nastiausenko.movies.review.exception.ReviewNotFoundException;
import dev.nastiausenko.movies.user.exception.EmailAlreadyTakenException;
import dev.nastiausenko.movies.user.exception.UserNotFoundException;
import dev.nastiausenko.movies.user.exception.UsernameAlreadyTakenException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.naming.AuthenticationException;
import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {
    //Already exist exceptions
    @ExceptionHandler({EmailAlreadyTakenException.class, UsernameAlreadyTakenException.class, BadCredentialsException.class,})
    public ResponseEntity<ExceptionResponse> handleBadRequestException(RuntimeException ex, HttpServletRequest request) {
        ExceptionResponse response = new ExceptionResponse(ex.getMessage(), HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI(), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    //Not found exceptions
    @ExceptionHandler({UserNotFoundException.class, ReviewNotFoundException.class, CategoryNotFoundException.class})
    public ResponseEntity<ExceptionResponse> handleNotFoundException(RuntimeException ex, HttpServletRequest request) {
        ExceptionResponse response = new ExceptionResponse(ex.getMessage(), HttpStatus.NOT_FOUND.value(),
                request.getRequestURI(), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    //unauthorized exception
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ExceptionResponse> handleAuthenticationException(RuntimeException ex, HttpServletRequest request) {
        ExceptionResponse response = new ExceptionResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED.value(),
                request.getRequestURI(), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(response);

    }

    //forbidden exception
    @ExceptionHandler({ForbiddenException.class, AdminRightsException.class})
    public ResponseEntity<ExceptionResponse> handleForbiddenException(RuntimeException ex, HttpServletRequest request) {
        ExceptionResponse response = new ExceptionResponse(ex.getMessage(), HttpStatus.FORBIDDEN.value(),
                request.getRequestURI(), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(response);
    }
}
