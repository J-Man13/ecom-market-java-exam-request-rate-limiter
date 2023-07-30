package com.example.ecommarketjavaexamrequestratelimiter.util;

import java.time.LocalDateTime;

public class CommonUtil {
    public static boolean dateIsBetween(LocalDateTime localDateTime, LocalDateTime from, LocalDateTime to){
        return localDateTime.isAfter(from) && localDateTime.isBefore(to);
    }
}
