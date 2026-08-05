package com.varshini.sentinel.transaction.exception;

import com.varshini.sentinel.transaction.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;


@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value=SameAccountTransferException.class)
    public ResponseEntity<ApiErrorResponse> handleSameAccountTransferException(SameAccountTransferException e, HttpServletRequest request){
        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .timeStamp(Instant.now())
                .httpStatus(HttpStatus.BAD_REQUEST)
                .error(HttpStatus.BAD_REQUEST.name())
                .message(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleException(Exception e, HttpServletRequest request){
        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .timeStamp(Instant.now())
                .httpStatus(HttpStatus.BAD_REQUEST)
                .error(HttpStatus.BAD_REQUEST.name())
                .message(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

}
