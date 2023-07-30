package com.example.ecommarketjavaexamrequestratelimiter.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/samples")
@Slf4j
public class SampleController {
    @GetMapping
    public void sample() {
        log.info("/samples requests processing");
    }
}
