package com.portfolio.saju.profile.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.saju.profile.domain.CalendarType;
import com.portfolio.saju.profile.domain.Gender;
import com.portfolio.saju.profile.dto.SajuAnalysisResult;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RuleBasedSajuAnalysisService implements SajuAnalysisService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${saju.profile-cache-ttl-hours}")
    private long cacheTtlHours;

    @Override
    public SajuAnalysisResult analyze(LocalDate birthDate, LocalTime birthTime, CalendarType calendarType, Gender gender) {
        String key = cacheKey(birthDate, birthTime, calendarType, gender);
        String cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, SajuAnalysisResult.class);
            } catch (JsonProcessingException ignored) {
                redisTemplate.delete(key);
            }
        }

        SajuAnalysisResult result = createMockAnalysis(birthDate, birthTime, calendarType, gender);
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(result), Duration.ofHours(cacheTtlHours));
        } catch (JsonProcessingException ignored) {
            // Cache failure must not block profile creation.
        }
        return result;
    }

    private SajuAnalysisResult createMockAnalysis(
            LocalDate birthDate,
            LocalTime birthTime,
            CalendarType calendarType,
            Gender gender
    ) {
        int selector = Math.floorMod(birthDate.getYear() + birthDate.getMonthValue() + birthTime.getHour(), 5);
        String element = List.of("목", "화", "토", "금", "수").get(selector);
        String calendarText = calendarType == CalendarType.SOLAR ? "양력" : "음력";

        return new SajuAnalysisResult(
                "%s 기준 입력으로 볼 때, 변화에 반응하는 속도와 자기 기준을 세우는 힘이 함께 드러납니다.".formatted(calendarText),
                "현재 MVP 분석에서는 %s 기운을 중심으로 균형을 해석합니다. 부족한 기운은 생활 리듬과 관계 방식에서 보완하는 관점으로 봅니다.".formatted(element),
                List.of("상황을 빠르게 정리하는 힘", "책임감을 바탕으로 꾸준히 밀고 가는 태도", "관계 속 신뢰를 쌓는 능력"),
                List.of("혼자 감당하려는 습관", "결정 전 과도한 고민", "컨디션이 흐트러질 때 말투가 단단해지는 경향"),
                List.of("올해 일과 관계에서 가장 신경 써야 할 부분은?", "나에게 맞는 커리어 방향은?", "중요한 선택을 앞두고 어떤 기준을 세우면 좋을까?")
        );
    }

    private String cacheKey(LocalDate birthDate, LocalTime birthTime, CalendarType calendarType, Gender gender) {
        return "saju:profile:%s:%s:%s:%s".formatted(birthDate, birthTime, calendarType, gender);
    }
}
