package com.example.ecommarketjavaexamrequestratelimiter.configuration;

import com.example.ecommarketjavaexamrequestratelimiter.exception.RateLimitExceededException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class CommonRestExceptionHandler {
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<Void> rateLimitExceededExceptionHandler(RateLimitExceededException e) {
        log.warn(e.getMessage(), e);
        return new ResponseEntity<>(HttpStatus.BAD_GATEWAY);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Void> runtimeExceptionHandler(RuntimeException e) {
        log.error(e.getMessage(), e);
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
