package com.example.ecommarketjavaexamrequestratelimiter.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class IncomingRequest {
    private final String path;
    private final String requesterIp;
    private LocalDateTime created;

    public IncomingRequest(String path, String requesterIp) {
        this.path = path;
        this.requesterIp = requesterIp;
    }
}
