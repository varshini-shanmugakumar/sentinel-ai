package com.varshini.sentinel.transaction.exception;

import com.varshini.sentinel.transaction.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;


@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value=SameAccountTransferException.class)
    public ResponseEntity<ApiErrorResponse> handleSameAccountTransferException(HttpServletRequest request){
        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .timeStamp(Instant.now())
                .httpStatus(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.name())
                .message("Source and Destination accounts cannot be the same")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception, HttpServletRequest request){
        List<FieldError> fieldErrors = exception.getBindingResult().getFieldErrors();
        String msg = fieldErrors.stream()
                    .map(error ->
                        error.getField() + " : " + error.getDefaultMessage())
                    .collect(Collectors.joining(", "));

        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .timeStamp(Instant.now())
                .httpStatus(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.name())
                .message(msg)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(TransactionNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleTransactionNotFoundException(
            TransactionNotFoundException exception,
            HttpServletRequest request){
            ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                    .timeStamp(Instant.now())
                    .httpStatus(HttpStatus.NOT_FOUND.value())
                    .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                    .message(exception.getMessage())
                    .path(request.getRequestURI())
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

}
