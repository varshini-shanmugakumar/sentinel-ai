package com.varshini.sentinel.transaction.exception;

import com.varshini.sentinel.transaction.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {
    GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldHandleSameAccountTransferException() {
        HttpServletRequest request = mock(HttpServletRequest.class);

        when(request.getRequestURI())
                .thenReturn("/api/v1/transactions");

        ResponseEntity<ApiErrorResponse> response =
                handler.handleSameAccountTransferException(request);

        assertEquals(
                HttpStatus.BAD_REQUEST,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertEquals(
                "Source and Destination accounts cannot be the same",
                response.getBody().getMessage()
        );

        assertEquals(
                "/api/v1/transactions",
                response.getBody().getPath()
        );
    }

    @Test
    void shouldHandleMultipleValidationErrors() {
        MethodArgumentNotValidException exception =
                mock(MethodArgumentNotValidException.class);

        BindingResult bindingResult = mock(BindingResult.class);

        FieldError amountError = new FieldError(
                "createTransactionRequest",
                "amount",
                "Amount must be greater than 0"
        );

        FieldError currencyError = new FieldError(
                "createTransactionRequest",
                "currency",
                "Currency is required"
        );

        when(exception.getBindingResult()).thenReturn(bindingResult);

        when(bindingResult.getFieldErrors())
                .thenReturn(List.of(amountError, currencyError));

        HttpServletRequest request = mock(HttpServletRequest.class);

        when(request.getRequestURI())
                .thenReturn("/api/v1/transactions");
        ResponseEntity<ApiErrorResponse> response =
                handler.handleMethodArgumentNotValidException(
                        exception,
                        request
                );
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void shouldHandleTransactionNotFoundException() {
        TransactionNotFoundException exception =
                new TransactionNotFoundException("Transaction not found");

        MockHttpServletRequest request =  new MockHttpServletRequest();
        request.setRequestURI("/api/v1/transactions/123");

        ResponseEntity<ApiErrorResponse> response =
                handler.handleTransactionNotFoundException(exception, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());

        ApiErrorResponse apiErrorResponse = response.getBody();

        assertEquals(404, apiErrorResponse.getHttpStatus());
        assertEquals("Not Found", apiErrorResponse.getError());
        assertEquals("Transaction not found", apiErrorResponse.getMessage());
        assertEquals("/api/v1/transactions/123", apiErrorResponse.getPath());
    }
}
