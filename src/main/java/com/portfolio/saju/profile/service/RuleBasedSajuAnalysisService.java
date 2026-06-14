package com.portfolio.saju.profile.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.saju.common.exception.BusinessException;
import com.portfolio.saju.common.exception.ErrorCode;
import com.portfolio.saju.profile.domain.CalendarType;
import com.portfolio.saju.profile.domain.Gender;
import com.portfolio.saju.profile.dto.SajuAnalysisResult;
import com.portfolio.saju.profile.manse.ManseCalendarResult;
import com.portfolio.saju.profile.manse.ManseCalendarService;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RuleBasedSajuAnalysisService implements SajuAnalysisService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final ManseCalendarService manseCalendarService;

    @Value("${saju.profile-cache-ttl-hours}")
    private long cacheTtlHours;

    @Override
    public SajuAnalysisResult analyze(
            LocalDate birthDate,
            LocalTime birthTime,
            CalendarType calendarType,
            Gender gender,
            boolean leapMonth
    ) {
        String key = cacheKey(birthDate, birthTime, calendarType, leapMonth, gender, ManseCalendarService.VERSION);
        String cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            try {
                SajuAnalysisResult cachedResult = objectMapper.readValue(cached, SajuAnalysisResult.class);
                if (hasCompleteDisplayData(cachedResult)) {
                    return cachedResult;
                }
                redisTemplate.delete(key);
            } catch (JsonProcessingException ignored) {
                redisTemplate.delete(key);
            }
        }

        SajuAnalysisResult result;
        try {
            result = createMockAnalysis(birthDate, birthTime, calendarType, gender, leapMonth);
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, exception.getMessage());
        }
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
            Gender gender,
            boolean leapMonth
    ) {
        SeasonType season = resolveSeason(birthDate);
        TimeEnergyType timeEnergy = resolveTimeEnergy(birthTime);
        ManseCalendarResult manse = calculateManseSafely(birthDate, birthTime, calendarType, leapMonth);

        return new SajuAnalysisResult(
                buildAnalysisSummary(season, timeEnergy, calendarType, manse),
                buildElementSummary(season, timeEnergy, calendarType, manse),
                buildStrengths(season, timeEnergy),
                buildCautions(season, timeEnergy),
                buildRecommendedQuestions(season, timeEnergy, manse),
                leapMonth,
                manse.yearPillar(),
                manse.monthPillar(),
                manse.dayPillar(),
                manse.hourPillar(),
                manse.fiveElements(),
                manse.yinYang(),
                manse.calculationStandard(),
                manse.warning(),
                manse.manseCalendarVersion()
        );
    }

    private ManseCalendarResult calculateManseSafely(
            LocalDate birthDate,
            LocalTime birthTime,
            CalendarType calendarType,
            boolean leapMonth
    ) {
        try {
            return manseCalendarService.calculate(birthDate, birthTime, calendarType, leapMonth);
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            log.warn(
                    "Failed to calculate base saju calendar. birthDate={}, birthTime={}, calendarType={}, leapMonth={}, error={}",
                    birthDate,
                    birthTime,
                    calendarType,
                    leapMonth,
                    exception.getMessage(),
                    exception
            );
            return new ManseCalendarResult(
                    "계산 실패",
                    "계산 실패",
                    "계산 실패",
                    "계산 실패",
                    Map.of(),
                    Map.of(),
                    "기본 계산 기준",
                    "참고: 현재 풀이는 기본 계산 기준으로 제공되며, 전문 감정과 차이가 있을 수 있습니다.",
                    ManseCalendarService.VERSION
            );
        }
    }

    private SeasonType resolveSeason(LocalDate birthDate) {
        int month = birthDate.getMonthValue();
        if (month >= 3 && month <= 5) {
            return SeasonType.SPRING;
        }
        if (month >= 6 && month <= 8) {
            return SeasonType.SUMMER;
        }
        if (month >= 9 && month <= 11) {
            return SeasonType.AUTUMN;
        }
        return SeasonType.WINTER;
    }

    private TimeEnergyType resolveTimeEnergy(LocalTime birthTime) {
        int hour = birthTime.getHour();
        if (hour >= 0 && hour < 6) {
            return TimeEnergyType.DAWN;
        }
        if (hour >= 6 && hour < 12) {
            return TimeEnergyType.MORNING;
        }
        if (hour >= 12 && hour < 18) {
            return TimeEnergyType.AFTERNOON;
        }
        return TimeEnergyType.NIGHT;
    }

    private String buildAnalysisSummary(
            SeasonType season,
            TimeEnergyType timeEnergy,
            CalendarType calendarType,
            ManseCalendarResult manse
    ) {
        String calendarPerspective = calendarType == CalendarType.SOLAR
                ? "양력 기준의 현실적 흐름을 중심으로 보면"
                : "음력 기준의 전통적 흐름을 중심으로 보면";

        return ("%s, 사주팔자는 년주 %s, 월주 %s, 일주 %s, 시주 %s로 계산되었습니다. "
                + "%s의 %s 성향과 %s의 %s 에너지가 함께 드러납니다. "
                + "새로운 일을 바라볼 때는 %s을 살리되, 실제 선택에서는 %s을 함께 점검하는 방식이 잘 맞습니다.")
                .formatted(
                        calendarPerspective,
                        manse.yearPillar(),
                        manse.monthPillar(),
                        manse.dayPillar(),
                        manse.hourPillar(),
                        season.label(),
                        season.theme(),
                        timeEnergy.label(),
                        timeEnergy.theme(),
                        season.primaryTrait(),
                        timeEnergy.decisionHint()
                );
    }

    private String buildElementSummary(
            SeasonType season,
            TimeEnergyType timeEnergy,
            CalendarType calendarType,
            ManseCalendarResult manse
    ) {
        String calendarTone = calendarType == CalendarType.SOLAR
                ? "실용적인 선택과 현재의 우선순위"
                : "내면의 리듬과 관계 속 균형";

        return ("현재 입력값 기준의 오행 분포는 %s이며, 음양 분포는 %s입니다. "
                + "생월 기반 보조 해석에서는 %s은 %s 기운과 연결되어 %s이 강점으로 나타나기 쉽고, "
                + "출생시간의 %s 성향은 %s을 더합니다. "
                + "따라서 %s를 기준으로 생활 리듬, 일의 방식, 관계의 속도를 조율하는 것이 좋습니다.")
                .formatted(
                        formatCounts(manse.fiveElements()),
                        formatCounts(manse.yinYang()),
                        season.label(),
                        season.element(),
                        season.elementMeaning(),
                        timeEnergy.label(),
                        timeEnergy.elementSupport(),
                        calendarTone
                );
    }

    private List<String> buildStrengths(SeasonType season, TimeEnergyType timeEnergy) {
        return List.of(
                season.strength(),
                timeEnergy.strength(),
                "%s과 %s을 함께 활용해 상황을 자기 방식으로 정리하는 힘".formatted(
                        season.primaryTrait(),
                        timeEnergy.primaryTrait()
                )
        );
    }

    private List<String> buildCautions(SeasonType season, TimeEnergyType timeEnergy) {
        return List.of(
                season.caution(),
                timeEnergy.caution(),
                "좋은 흐름을 느껴도 결정을 서두르기보다 기준, 일정, 감정 소모를 함께 점검하기"
        );
    }

    private List<String> buildRecommendedQuestions(SeasonType season, TimeEnergyType timeEnergy, ManseCalendarResult manse) {
        return List.of(
                "제 사주팔자 %s/%s/%s/%s에서 강하게 볼 수 있는 흐름은 무엇인가요?".formatted(
                        manse.yearPillar(),
                        manse.monthPillar(),
                        manse.dayPillar(),
                        manse.hourPillar()
                ),
                "%s을 살릴 수 있는 업무 방식은 무엇인가요?".formatted(season.primaryTrait()),
                "제가 의사결정할 때 %s을 어떻게 보완하면 좋을까요?".formatted(timeEnergy.cautionKeyword()),
                "%s와 잘 맞는 커리어 방향은 무엇인가요?".formatted(timeEnergy.primaryTrait())
        );
    }

    String cacheKey(
            LocalDate birthDate,
            LocalTime birthTime,
            CalendarType calendarType,
            boolean leapMonth,
            Gender gender,
            String manseCalendarVersion
    ) {
        return "saju:analysis:v3:manse:%s:%s:%s:%s:%s:%s".formatted(
                birthDate,
                birthTime,
                calendarType,
                leapMonth,
                gender,
                manseCalendarVersion
        );
    }

    private String formatCounts(Map<String, Integer> counts) {
        return counts.entrySet().stream()
                .map(entry -> entry.getKey() + " " + entry.getValue())
                .reduce((left, right) -> left + ", " + right)
                .orElse("-");
    }

    private boolean hasCompleteDisplayData(SajuAnalysisResult result) {
        return result != null
                && hasText(result.yearPillar())
                && hasText(result.monthPillar())
                && hasText(result.dayPillar())
                && hasText(result.hourPillar())
                && result.fiveElements() != null
                && !result.fiveElements().isEmpty()
                && result.yinYang() != null
                && !result.yinYang().isEmpty()
                && hasText(result.calculationStandard())
                && hasText(result.calculationWarning())
                && !containsLegacyDisplayTerm(result.analysisSummary())
                && !containsLegacyDisplayTerm(result.elementSummary())
                && !containsLegacyDisplayTerm(result.calculationStandard())
                && !containsLegacyDisplayTerm(result.calculationWarning());
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private boolean containsLegacyDisplayTerm(String value) {
        return value != null && value.contains("MVP");
    }

    private enum SeasonType {
        SPRING(
                "봄",
                "성장과 시작",
                "목",
                "기획, 확장, 새로운 시도",
                "성장 감각",
                "아이디어를 빠르게 발견하고 가능성을 넓히는 힘",
                "시작은 빠르지만 마무리 기준이 흐려질 수 있음"
        ),
        SUMMER(
                "여름",
                "표현과 추진",
                "화",
                "열정, 대외활동, 표현력",
                "추진력",
                "분위기를 만들고 사람 앞에서 에너지를 전달하는 힘",
                "속도가 빨라질수록 세부 확인이 느슨해질 수 있음"
        ),
        AUTUMN(
                "가을",
                "정리와 성과",
                "금",
                "판단, 현실감, 결과 정리",
                "현실 감각",
                "복잡한 상황을 기준에 따라 정리하고 성과로 연결하는 힘",
                "판단이 선명한 만큼 스스로와 타인에게 엄격해질 수 있음"
        ),
        WINTER(
                "겨울",
                "탐구와 축적",
                "수",
                "신중함, 내면 집중, 깊이 있는 탐색",
                "탐구심",
                "충분히 관찰하고 깊게 파고들어 본질을 찾는 힘",
                "생각이 깊어질수록 실행 타이밍을 놓칠 수 있음"
        );

        private final String label;
        private final String theme;
        private final String element;
        private final String elementMeaning;
        private final String primaryTrait;
        private final String strength;
        private final String caution;

        SeasonType(
                String label,
                String theme,
                String element,
                String elementMeaning,
                String primaryTrait,
                String strength,
                String caution
        ) {
            this.label = label;
            this.theme = theme;
            this.element = element;
            this.elementMeaning = elementMeaning;
            this.primaryTrait = primaryTrait;
            this.strength = strength;
            this.caution = caution;
        }

        String label() {
            return label;
        }

        String theme() {
            return theme;
        }

        String element() {
            return element;
        }

        String elementMeaning() {
            return elementMeaning;
        }

        String primaryTrait() {
            return primaryTrait;
        }

        String strength() {
            return strength;
        }

        String caution() {
            return caution;
        }
    }

    private enum TimeEnergyType {
        DAWN(
                "새벽",
                "관찰력과 내면 집중",
                "관찰력",
                "조용히 흐름을 읽고 리스크를 먼저 감지하는 힘",
                "혼자 오래 판단하다가 표현이 늦어질 수 있음",
                "생각을 밖으로 꺼내는 타이밍",
                "깊이 있는 분석"
        ),
        MORNING(
                "오전",
                "계획성과 시작 에너지",
                "실행력",
                "계획을 세우고 초반 추진력을 만드는 힘",
                "계획이 어긋날 때 유연성이 떨어질 수 있음",
                "계획 변경에 대한 유연성",
                "계획형 실행"
        ),
        AFTERNOON(
                "오후",
                "대인관계와 활동성",
                "소통력",
                "사람들과 맞물려 움직이며 기회를 넓히는 힘",
                "외부 반응에 따라 에너지 소모가 커질 수 있음",
                "관계 속 에너지 소모",
                "협업과 커뮤니케이션"
        ),
        NIGHT(
                "저녁/밤",
                "몰입과 깊이 있는 사고",
                "몰입력",
                "한 가지 주제를 오래 붙잡고 완성도를 높이는 힘",
                "몰입이 깊어질수록 휴식과 전환이 늦어질 수 있음",
                "휴식과 전환의 균형",
                "전문성 강화"
        );

        private final String label;
        private final String theme;
        private final String primaryTrait;
        private final String strength;
        private final String caution;
        private final String cautionKeyword;
        private final String elementSupport;

        TimeEnergyType(
                String label,
                String theme,
                String primaryTrait,
                String strength,
                String caution,
                String cautionKeyword,
                String elementSupport
        ) {
            this.label = label;
            this.theme = theme;
            this.primaryTrait = primaryTrait;
            this.strength = strength;
            this.caution = caution;
            this.cautionKeyword = cautionKeyword;
            this.elementSupport = elementSupport;
        }

        String label() {
            return label;
        }

        String theme() {
            return theme;
        }

        String primaryTrait() {
            return primaryTrait;
        }

        String strength() {
            return strength;
        }

        String caution() {
            return caution;
        }

        String cautionKeyword() {
            return cautionKeyword;
        }

        String elementSupport() {
            return elementSupport;
        }

        String decisionHint() {
            return cautionKeyword;
        }
    }
}
