package com.example.ecommarketjavaexamrequestratelimiter.security.rate.limiter;

import com.example.ecommarketjavaexamrequestratelimiter.model.IncomingRequest;
import com.example.ecommarketjavaexamrequestratelimiter.service.RateLimitService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Slf4j
public class RateLimiterFilter extends OncePerRequestFilter {
    private final HandlerExceptionResolver resolver;
    private final RateLimitService rateLimitService;
    public RateLimiterFilter(HandlerExceptionResolver resolver,
                             RateLimitService rateLimitService) {
        this.resolver = resolver;
        this.rateLimitService = rateLimitService;
    }

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        log.info("rate limiter filter processing");
        try {
            rateLimitService.saveIncomingRequest(new IncomingRequest(request.getRequestURI(), request.getRemoteAddr()));
            chain.doFilter(request, response);
        }
        catch (Exception e){
            resolver.resolveException(request, response, null, e);
        }
    }
}
