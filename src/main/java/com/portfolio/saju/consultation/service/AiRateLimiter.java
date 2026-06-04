package com.portfolio.saju.consultation.service;

import com.portfolio.saju.common.exception.BusinessException;
import com.portfolio.saju.common.exception.ErrorCode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AiRateLimiter {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;

    private final StringRedisTemplate redisTemplate;

    @Value("${saju.ai-limit-per-day}")
    private long limitPerDay;

    public void checkAndIncrease(Long userId) {
        String key = "ai:limit:%d:%s".formatted(userId, LocalDate.now().format(DATE_FORMAT));
        Long count = redisTemplate.opsForValue().increment(key);

        if (count != null && count == 1L) {
            redisTemplate.expire(key, ttlUntilEndOfDay());
        }

        if (count != null && count > limitPerDay) {
            throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS, "Daily AI consultation limit exceeded");
        }
    }

    private Duration ttlUntilEndOfDay() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endOfDay = LocalDateTime.of(now.toLocalDate().plusDays(1), LocalTime.MIDNIGHT);
        return Duration.between(now, endOfDay);
    }
}
