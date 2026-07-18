package com.eai.api.common;

import com.eai.application.common.ApplicationException;
import com.eai.application.common.ConflictException;
import com.eai.application.common.ForbiddenException;
import com.eai.application.common.NotFoundException;
import com.eai.application.common.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Locale;

@RestControllerAdvice
public class ApiExceptionHandler {

    private final MessageSource messageSource;

    public ApiExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ErrorResponse> handleApplicationException(ApplicationException exception) {
        return ResponseEntity.status(statusFor(exception))
                .body(new ErrorResponse(exception.getCode(), translateMessage(exception.getMessage()), Instant.now()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> translateField(error.getField()) + " " + translateMessage(error.getDefaultMessage()))
                .orElse(resolve("error.invalid_request", "Requisição inválida"));
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("VALIDATION_ERROR", message, Instant.now()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException exception) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("VALIDATION_ERROR", translateMessage(exception.getMessage()), Instant.now()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse("FORBIDDEN", "Acesso negado", Instant.now()));
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

    private String translateMessage(String message) {
        if (message == null || message.isBlank()) {
            return resolve("error.unexpected", "Erro inesperado");
        }
        String dynamicMessage = translateDynamicMessage(message);
        if (!dynamicMessage.equals(message)) {
            return dynamicMessage;
        }
        return resolve("error." + normalizeKey(message), message);
    }

    private String translateDynamicMessage(String message) {
        if (message.startsWith("companyId is required for role ")) {
            return resolve("error.company_id_required_for_role", "Empresa é obrigatória para o papel {0}", message.substring("companyId is required for role ".length()));
        }
        if (message.startsWith("companyId is required role ")) {
            return resolve("error.company_id_required_for_role", "Empresa é obrigatória para o papel {0}", message.substring("companyId is required role ".length()));
        }
        if (message.startsWith("storeId is required for role ")) {
            return resolve("error.store_id_required_for_role", "Loja é obrigatória para o papel {0}", message.substring("storeId is required for role ".length()));
        }
        if (message.startsWith("storeId is required role ")) {
            return resolve("error.store_id_required_for_role", "Loja é obrigatória para o papel {0}", message.substring("storeId is required role ".length()));
        }
        if (message.endsWith(" is required")) {
            return resolve("error.field_required", "{0} é obrigatório", translateField(message.substring(0, message.length() - " is required".length())));
        }
        if (message.endsWith(" must not be null")) {
            return resolve("error.field_required", "{0} é obrigatório", translateField(message.substring(0, message.length() - " must not be null".length())));
        }
        if (message.endsWith(" must not be blank")) {
            return resolve("error.field_required", "{0} é obrigatório", translateField(message.substring(0, message.length() - " must not be blank".length())));
        }
        return message;
    }

    private String translateField(String field) {
        return resolve("field." + field, field);
    }

    private String resolve(String code, String fallback, Object... args) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(code, args, fallback, locale);
    }

    private String normalizeKey(String value) {
        return value.trim()
                .replaceAll("([a-z])([A-Z])", "$1_$2")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
    }
}
