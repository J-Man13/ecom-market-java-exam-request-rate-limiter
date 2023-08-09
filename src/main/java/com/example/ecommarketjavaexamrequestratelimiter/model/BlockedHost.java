package com.example.ecommarketjavaexamrequestratelimiter.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class BlockedHost {
    private String ip;
    private LocalDateTime created;
}
