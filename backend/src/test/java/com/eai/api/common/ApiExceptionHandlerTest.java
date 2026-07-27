package com.eai.api.common;

import com.eai.application.common.ApplicationException;
import com.eai.application.common.ConflictException;
import com.eai.application.common.ForbiddenException;
import com.eai.application.common.NotFoundException;
import com.eai.application.common.UnauthorizedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler(messageSource());

    @DisplayName("Traduz mensagens conhecidas com acentos")
    @Test
    void translatesKnownMessagesWithAccents() {
        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgumentException(
                new IllegalArgumentException("Cannot deactivate company with active users")
        );

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Não é possível inativar empresa com usuários ativos");
    }

    @DisplayName("Traduz mensagens dinamicas de campo obrigatorio com acentos")
    @Test
    void translatesDynamicRequiredFieldMessagesWithAccents() {
        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgumentException(
                new IllegalArgumentException("document is required")
        );

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Documento é obrigatório");
    }

    @DisplayName("Mapeia excecoes de aplicacao para os status HTTP esperados")
    @Test
    void mapsApplicationExceptionsToHttpStatus() {
        assertThat(handler.handleApplicationException(new NotFoundException("Lead not found")).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(handler.handleApplicationException(new ConflictException("Lead already assigned")).getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(handler.handleApplicationException(new UnauthorizedException("Invalid credentials")).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(handler.handleApplicationException(new ForbiddenException("Access denied")).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(handler.handleApplicationException(new ApplicationException("APP", "custom error")).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @DisplayName("Traduz mensagens dinamicas de empresa e loja obrigatorias por papel")
    @Test
    void translatesDynamicRoleTenantMessages() {
        ResponseEntity<ErrorResponse> company = handler.handleIllegalArgumentException(
                new IllegalArgumentException("companyId is required for role MANAGER")
        );
        ResponseEntity<ErrorResponse> store = handler.handleIllegalArgumentException(
                new IllegalArgumentException("storeId is required role SELLER")
        );

        assertThat(company.getBody()).isNotNull();
        assertThat(company.getBody().message()).contains("MANAGER");
        assertThat(store.getBody()).isNotNull();
        assertThat(store.getBody().message()).contains("SELLER");
    }

    @DisplayName("Mensagem vazia usa fallback de erro inesperado")
    @Test
    void blankMessageUsesUnexpectedFallback() {
        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgumentException(new IllegalArgumentException(" "));

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Erro inesperado");
    }

    @DisplayName("Acesso negado do Spring retorna erro forbidden padronizado")
    @Test
    void accessDeniedReturnsForbiddenResponse() {
        ResponseEntity<ErrorResponse> response = handler.handleAccessDeniedException(new AccessDeniedException("denied"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("FORBIDDEN");
    }

    @DisplayName("Erro de validacao usa primeiro campo traduzido")
    @Test
    void validationErrorUsesFirstTranslatedField() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "email", "must not be blank"));
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleValidationException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getBody().message()).contains("E-mail");
    }

    @DisplayName("Erro de validacao sem campo usa fallback de requisicao invalida")
    @Test
    void validationErrorWithoutFieldsUsesFallback() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleValidationException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Requisição inválida");
    }

    private ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasename("messages");
        source.setDefaultEncoding("UTF-8");
        source.setDefaultLocale(Locale.forLanguageTag("pt-BR"));
        return source;
    }
}
