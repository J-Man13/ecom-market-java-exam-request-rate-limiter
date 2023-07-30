package com.example.ecommarketjavaexamrequestratelimiter.model.entity.reddis;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

import java.util.concurrent.TimeUnit;

@RedisHash("BlockedHost")
@Getter
@Setter
public class BlockedHost {
    @Id
    private Long id;
    @Indexed
    private String ip;
    @TimeToLive(unit = TimeUnit.MINUTES)
    private Long ttlInMinutes;
}
