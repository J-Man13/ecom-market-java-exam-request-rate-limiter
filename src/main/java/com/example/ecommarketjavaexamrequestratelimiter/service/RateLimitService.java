package com.example.ecommarketjavaexamrequestratelimiter.service;

import com.example.ecommarketjavaexamrequestratelimiter.exception.RateLimitExceededException;
import com.example.ecommarketjavaexamrequestratelimiter.model.entity.reddis.BlockedHost;
import com.example.ecommarketjavaexamrequestratelimiter.model.entity.reddis.IncomingRequest;
import com.example.ecommarketjavaexamrequestratelimiter.repository.BlockedHostRepository;
import com.example.ecommarketjavaexamrequestratelimiter.repository.IncomingRequestRepository;
import com.example.ecommarketjavaexamrequestratelimiter.util.CommonUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class RateLimitService {
    private final IncomingRequestRepository incomingRequestRepository;
    private final BlockedHostRepository blockedHostRepository;
    private final Long rateLimit;
    private final Long rateLimitPeriodMinutes;
    private final Long blockHostPeriodMinutes;


    public RateLimitService(IncomingRequestRepository incomingRequestRepository,
                            BlockedHostRepository blockedHostRepository,
                            @Value("${rate.limit}") long rateLimit,
                            @Value("${rate.limit.period.minutes}") long rateLimitPeriodMinutes,
                            @Value("${host.block.period.minutes}") long blockHostPeriodMinutes) {
        this.incomingRequestRepository = incomingRequestRepository;
        this.blockedHostRepository = blockedHostRepository;
        this.rateLimit = rateLimit;
        this.rateLimitPeriodMinutes = rateLimitPeriodMinutes;
        this.blockHostPeriodMinutes = blockHostPeriodMinutes;
    }

    public IncomingRequest saveIncomingRequest(IncomingRequest incomingRequest) {
        if (blockedHostRepository.findByIp(incomingRequest.getRequesterIp()).isPresent()) {
            throw new RateLimitExceededException("Rate limit has been exceeded for requesting host");
        }
        long numberOfAttemptsHostToPathPerPeriod = incomingRequestRepository.findByPathAndRequesterIp(
                        incomingRequest.getPath(),
                        incomingRequest.getRequesterIp()
                )
                .stream()
                .filter(request -> CommonUtil.dateIsBetween(
                        request.getCreated(),
                        LocalDateTime.now().minusMinutes(rateLimitPeriodMinutes),
                        LocalDateTime.now()
                ))
                .count();
        if (numberOfAttemptsHostToPathPerPeriod >= rateLimit) {
            BlockedHost blockedHost = new BlockedHost();
            blockedHost.setIp(incomingRequest.getRequesterIp());
            blockedHost.setTtlInMinutes(blockHostPeriodMinutes);
            blockedHostRepository.save(blockedHost);
        }
        incomingRequest.setCreated(LocalDateTime.now());
        incomingRequest.setTtlInMinutes(rateLimitPeriodMinutes);
        return incomingRequestRepository.save(incomingRequest);
    }
}
