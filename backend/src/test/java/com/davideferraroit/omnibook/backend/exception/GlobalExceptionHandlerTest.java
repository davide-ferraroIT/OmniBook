package com.davideferraroit.omnibook.backend.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleResourceNotFoundException_ShouldReturn404() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Not found");
        ProblemDetail result = handler.handleResourceNotFoundException(ex);

        assertThat(result.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(result.getDetail()).isEqualTo("Not found");
    }

    @Test
    void handleSlugAlreadyExistsException_ShouldReturn409() {
        SlugAlreadyExistsException ex = new SlugAlreadyExistsException("Conflict");
        ProblemDetail result = handler.handleSlugAlreadyExistsException(ex);

        assertThat(result.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(result.getDetail()).isEqualTo("Conflict");
    }

    @Test
    void handleValidationException_ShouldReturn400WithFields() {
        org.springframework.core.MethodParameter parameter = mock(org.springframework.core.MethodParameter.class);
        BindingResult bindingResult = new org.springframework.validation.BeanPropertyBindingResult(new Object(), "objectName");
        bindingResult.addError(new FieldError("objectName", "email", "must not be blank"));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);

        ProblemDetail result = handler.handleValidationException(ex);

        assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(result.getDetail()).contains("email: must not be blank");
    }

    @Test
    void handleDomainExceptions_ShouldReturn400() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid state");
        ProblemDetail result = handler.handleDomainExceptions(ex);

        assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(result.getDetail()).isEqualTo("Invalid state");
    }

    @Test
    void handleAuthenticationException_ShouldReturn401() {
        AuthenticationException ex = new AuthenticationException("Bad creds") {};
        ProblemDetail result = handler.handleAuthenticationException(ex);

        assertThat(result.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(result.getDetail()).contains("non corretti");
    }

    @Test
    void handleGenericException_ShouldReturn500() {
        Exception ex = new Exception("Critical failure");
        ProblemDetail result = handler.handleGenericException(ex);

        assertThat(result.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(result.getDetail()).isEqualTo("Si è verificato un errore imprevisto.");
    }
}
