package com.portfolio.saju.consultation.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.portfolio.saju.common.exception.BusinessException;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

class AiRateLimiterTest {

    @Test
    void firstRequestSetsExpire() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn(1L);
        AiRateLimiter limiter = new AiRateLimiter(redisTemplate);
        ReflectionTestUtils.setField(limiter, "limitPerDay", 20L);

        limiter.checkAndIncrease(1L);

        verify(redisTemplate).expire(anyString(), any(Duration.class));
    }

    @Test
    void exceedingLimitThrowsBusinessException() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn(21L);
        AiRateLimiter limiter = new AiRateLimiter(redisTemplate);
        ReflectionTestUtils.setField(limiter, "limitPerDay", 20L);

        assertThatThrownBy(() -> limiter.checkAndIncrease(1L))
                .isInstanceOf(BusinessException.class);
    }
}
