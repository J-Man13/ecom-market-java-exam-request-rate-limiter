package com.example.ecommarketjavaexamrequestratelimiter.model.entity.reddis;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@RedisHash("IncomingRequest")
@Getter
@Setter
public class IncomingRequest {
    private Long id;
    @Indexed
    private final String path;
    @Indexed
    private final String requesterIp;
    private LocalDateTime created;
    @TimeToLive(unit = TimeUnit.MINUTES)
    private Long ttlInMinutes;

    public IncomingRequest(String path, String requesterIp) {
        this.path = path;
        this.requesterIp = requesterIp;
    }
}
