package com.eai.api.common;

import com.eai.application.common.ApplicationException;
import com.eai.application.common.ConflictException;
import com.eai.application.common.ForbiddenException;
import com.eai.application.common.NotFoundException;
import com.eai.application.common.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ErrorResponse> handleApplicationException(ApplicationException exception) {
        return ResponseEntity.status(statusFor(exception))
                .body(new ErrorResponse(exception.getCode(), exception.getMessage(), Instant.now()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .orElse("Invalid request");
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("VALIDATION_ERROR", message, Instant.now()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException exception) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("VALIDATION_ERROR", exception.getMessage(), Instant.now()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse("FORBIDDEN", "Access denied", Instant.now()));
    }

    private HttpStatus statusFor(ApplicationException exception) {
        if (exception instanceof NotFoundException) {
            return HttpStatus.NOT_FOUND;
        }
        if (exception instanceof ConflictException) {
            return HttpStatus.CONFLICT;
        }
        if (exception instanceof UnauthorizedException) {
            return HttpStatus.UNAUTHORIZED;
        }
        if (exception instanceof ForbiddenException) {
            return HttpStatus.FORBIDDEN;
        }
        return HttpStatus.BAD_REQUEST;
    }
}
