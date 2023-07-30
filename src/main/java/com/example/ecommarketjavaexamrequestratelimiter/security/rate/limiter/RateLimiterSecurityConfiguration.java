package com.example.ecommarketjavaexamrequestratelimiter.security.rate.limiter;

import com.example.ecommarketjavaexamrequestratelimiter.service.RateLimitService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;


@Configuration
@EnableWebSecurity
public class RateLimiterSecurityConfiguration {
    private final String[] arrayOfStrings;
    private final RateLimiterFilter rateLimiterFilter;


    public RateLimiterSecurityConfiguration(@Value("${rate.limited.endpoints}") String[] arrayOfStrings,
                                            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver,
                                            RateLimitService rateLimitService) {
        this.arrayOfStrings = arrayOfStrings;
        this.rateLimiterFilter = new RateLimiterFilter(resolver, rateLimitService);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .headers(httpSecurityHeadersConfigurer -> httpSecurityHeadersConfigurer.httpStrictTransportSecurity(HeadersConfigurer.HstsConfig::disable))
                .httpBasic(AbstractHttpConfigurer::disable)
                .securityMatcher(arrayOfStrings)
                .addFilterAt(rateLimiterFilter, BasicAuthenticationFilter.class)
                .sessionManagement(manager -> manager.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }



}
