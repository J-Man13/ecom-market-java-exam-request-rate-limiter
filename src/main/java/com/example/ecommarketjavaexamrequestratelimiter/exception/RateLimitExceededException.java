package com.example.ecommarketjavaexamrequestratelimiter.exception;

public class RateLimitExceededException extends RuntimeException{
    public RateLimitExceededException(String message) {
        super(message);
    }
}
