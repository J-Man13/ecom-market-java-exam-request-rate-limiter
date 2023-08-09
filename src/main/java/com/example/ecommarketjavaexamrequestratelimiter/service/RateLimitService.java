package com.example.ecommarketjavaexamrequestratelimiter.service;

import com.example.ecommarketjavaexamrequestratelimiter.exception.RateLimitExceededException;
import com.example.ecommarketjavaexamrequestratelimiter.model.BlockedHost;
import com.example.ecommarketjavaexamrequestratelimiter.model.IncomingRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.*;

@Service
@Slf4j
public class RateLimitService {
    private final ConcurrentLinkedQueue<IncomingRequest> incomingRequests;
    private final ConcurrentHashMap<String,BlockedHost> ipBlockedHostMap;
    private final Long rateLimit;
    private final Long rateLimitPeriodMinutes;
    private final Long hostBlockPeriodMinutes;


    public RateLimitService(@Value("${rate.limit}") long rateLimit,
                            @Value("${rate.limit.period.minutes}") long rateLimitPeriodMinutes,
                            @Value("${host.block.period.minutes}") long hostBlockPeriodMinutes) {
        incomingRequests = new ConcurrentLinkedQueue<>();
        ipBlockedHostMap = new ConcurrentHashMap<>();
        this.rateLimit = rateLimit;
        this.rateLimitPeriodMinutes = rateLimitPeriodMinutes;
        this.hostBlockPeriodMinutes = hostBlockPeriodMinutes;
    }

    public IncomingRequest saveIncomingRequest(IncomingRequest incomingRequest) {
        String requesterIp = incomingRequest.getRequesterIp();
        String path = incomingRequest.getPath();
        if (ipBlockedHostMap.containsKey(requesterIp)) {
            throw new RateLimitExceededException("Rate limit has been exceeded for requesting host");
        }
        incomingRequest.setCreated(LocalDateTime.now());
        incomingRequests.add(incomingRequest);
        long numberOfAttemptsHostToPathPerPeriod = incomingRequests.stream()
                .filter(request -> request.getRequesterIp().equals(requesterIp)
                        && request.getPath().equals(path)
                        && request.getCreated().plusMinutes(rateLimitPeriodMinutes).isAfter(LocalDateTime.now())
                )
                .count();
        if (numberOfAttemptsHostToPathPerPeriod >= rateLimit) {
            BlockedHost blockedHost = new BlockedHost();
            blockedHost.setIp(requesterIp);
            blockedHost.setCreated(LocalDateTime.now());
            ipBlockedHostMap.put(requesterIp, blockedHost);
        }
        return incomingRequest;
    }

    @Scheduled(fixedDelayString = "${rate.limit.period.minutes}", timeUnit = TimeUnit.MINUTES)
    public void cleanUpIncomingRequests() {
        log.info("performing clean up of incoming requests");
        incomingRequests.removeIf(request -> request.getCreated().plusMinutes(rateLimitPeriodMinutes).isBefore(LocalDateTime.now()));
    }

    @Scheduled(fixedDelayString = "${host.block.period.minutes}", timeUnit = TimeUnit.MINUTES)
    public void cleanUpBlockedHosts() {
        log.info("performing clean up of blocked hosts");
        ipBlockedHostMap.entrySet().removeIf(entry -> entry.getValue().getCreated().plusMinutes(hostBlockPeriodMinutes).isBefore(LocalDateTime.now()));
    }
}
