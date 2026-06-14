# API 명세

Base URL: `http://localhost:8080`

보호 API는 다음 인증 헤더가 필요합니다.

```text
Authorization: Bearer {accessToken}
```

## 화면 URL

- `GET /view/signup`
- `GET /view/login`
- `GET /view/profiles`
- `GET /view/profiles/new`
- `GET /view/profiles/{profileId}`
- `GET /view/profiles/{profileId}/consultations`

## Auth

### POST `/api/auth/signup`

인증: 불필요

요청:

```json
{
  "email": "user@example.com",
  "password": "password123",
  "nickname": "yunji"
}
```

응답:

```json
{
  "accessToken": "jwt-token",
  "tokenType": "Bearer"
}
```

### POST `/api/auth/login`

인증: 불필요

요청:

```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

응답:

```json
{
  "accessToken": "jwt-token",
  "tokenType": "Bearer"
}
```

## Saju Profile

### POST `/api/profiles`

인증: 필요

요청:

```json
{
  "profileName": "내 프로필",
  "birthDate": "1998-03-15",
  "birthTime": "09:30",
  "calendarType": "SOLAR",
  "gender": "FEMALE",
  "leapMonth": false
}
```

요청 필드 허용값:

- `birthTime`: `HH:mm`
- `calendarType`: `SOLAR`, `LUNAR`
- `gender`: `MALE`, `FEMALE`
- `leapMonth`: 음력 윤달이면 `true`, 양력이면 `false`

응답:

```json
{
  "id": 1,
  "profileName": "내 프로필",
  "birthDate": "1998-03-15",
  "birthTime": "09:30:00",
  "calendarType": "SOLAR",
  "gender": "FEMALE",
  "birthPlace": null,
  "leapMonth": false,
  "analysisSummary": "양력 기준의 현실적 흐름을 중심으로 보면, 사주팔자는 년주 무인, 월주 을묘, 일주 신유, 시주 임진으로 계산되었습니다...",
  "elementSummary": "MVP 만세력 계산의 오행 분포는 목 2, 화 0, 토 2, 금 2, 수 2이며...",
  "strengths": ["아이디어를 빠르게 발견하고 가능성을 넓히는 힘"],
  "cautions": ["시작은 빠르지만 마무리 기준이 흐려질 수 있음"],
  "recommendedQuestions": ["제 사주팔자 무인/을묘/신유/임진에서 강하게 볼 수 있는 흐름은 무엇인가요?"],
  "yearPillar": "무인",
  "monthPillar": "을묘",
  "dayPillar": "신유",
  "hourPillar": "임진",
  "fiveElementsSummary": ["목: 2", "화: 0", "토: 2", "금: 2", "수: 2"],
  "yinYangSummary": ["양: 4", "음: 4"],
  "calculationStandard": "Asia/Seoul, 입춘 기준 년주, 절기 경계 근사 월주, 1900-01-31 갑진 기준 일주, 야자시 미적용",
  "calculationWarning": "전문 만세력 검증용이 아닌 포트폴리오 MVP 계산입니다.",
  "manseCalendarVersion": "manse-v1-mvp",
  "createdAt": "2026-06-04T15:00:00",
  "updatedAt": "2026-06-04T15:00:00"
}
```

분석 결과 참고:

- 현재는 완전한 만세력 계산이 아니라 MVP용 기본 만세력 계산과 규칙 기반 풀이를 조합합니다.
- 생년월일, 출생시간, 양력/음력, 윤달 선택값을 기반으로 사주팔자, 오행/음양 분포, 풀이 문장과 추천 질문을 생성합니다.
- 향후 실제 만세력, 오행, 십성, 대운/세운 계산으로 확장할 수 있는 구조입니다.

### GET `/api/profiles`

인증: 필요

응답:

```json
[
  {
    "id": 1,
    "profileName": "내 프로필",
    "birthDate": "1998-03-15",
    "birthTime": "09:30:00",
    "calendarType": "SOLAR",
    "gender": "FEMALE",
    "birthPlace": null,
    "leapMonth": false,
    "analysisSummary": "양력 기준의 현실적 흐름을 중심으로 보면...",
    "elementSummary": "MVP 규칙 기반 분석에서는...",
    "strengths": ["아이디어를 빠르게 발견하고 가능성을 넓히는 힘"],
    "cautions": ["시작은 빠르지만 마무리 기준이 흐려질 수 있음"],
    "recommendedQuestions": ["성장 감각을 살릴 수 있는 업무 방식은 무엇인가요?"],
    "yearPillar": "무인",
    "monthPillar": "을묘",
    "dayPillar": "신유",
    "hourPillar": "임진",
    "fiveElementsSummary": ["목: 2", "화: 0", "토: 2", "금: 2", "수: 2"],
    "yinYangSummary": ["양: 4", "음: 4"],
    "calculationStandard": "Asia/Seoul, 입춘 기준 년주, 절기 경계 근사 월주, 1900-01-31 갑진 기준 일주, 야자시 미적용",
    "calculationWarning": "전문 만세력 검증용이 아닌 포트폴리오 MVP 계산입니다.",
    "manseCalendarVersion": "manse-v1-mvp",
    "createdAt": "2026-06-04T15:00:00",
    "updatedAt": "2026-06-04T15:00:00"
  }
]
```

### GET `/api/profiles/{profileId}`

인증: 필요

응답: `POST /api/profiles` 응답과 동일한 구조

### POST `/api/profiles/{profileId}/reanalyze`

인증: 필요

요청 헤더:

```text
Authorization: Bearer {accessToken}
```

요청 body: 없음

동작:

- 기존 프로필의 `birthDate`, `birthTime`, `calendarType`, `gender`, `leapMonth`를 기준으로 최신 `SajuAnalysisService` 분석을 다시 실행합니다.
- `analysisSummary`, `elementSummary`, `strengths`, `cautions`, `recommendedQuestions`, 사주팔자, 오행/음양 분포, 계산 기준/주의 문구를 갱신합니다.
- `profileName`, `birthDate`, `birthTime`, `calendarType`, `gender`, 상담 기록은 변경하지 않습니다.

응답:

```json
{
  "id": 1,
  "profileName": "내 프로필",
  "birthDate": "1998-03-15",
  "birthTime": "09:30:00",
  "calendarType": "SOLAR",
  "gender": "FEMALE",
  "birthPlace": null,
  "leapMonth": false,
  "analysisSummary": "양력 기준의 현실적 흐름을 중심으로 보면...",
  "elementSummary": "MVP 규칙 기반 분석에서는...",
  "strengths": ["아이디어를 빠르게 발견하고 가능성을 넓히는 힘"],
  "cautions": ["시작은 빠르지만 마무리 기준이 흐려질 수 있음"],
  "recommendedQuestions": ["성장 감각을 살릴 수 있는 업무 방식은 무엇인가요?"],
  "yearPillar": "무인",
  "monthPillar": "을묘",
  "dayPillar": "신유",
  "hourPillar": "임진",
  "fiveElementsSummary": ["목: 2", "화: 0", "토: 2", "금: 2", "수: 2"],
  "yinYangSummary": ["양: 4", "음: 4"],
  "calculationStandard": "Asia/Seoul, 입춘 기준 년주, 절기 경계 근사 월주, 1900-01-31 갑진 기준 일주, 야자시 미적용",
  "calculationWarning": "전문 만세력 검증용이 아닌 포트폴리오 MVP 계산입니다.",
  "manseCalendarVersion": "manse-v1-mvp",
  "createdAt": "2026-06-04T15:00:00",
  "updatedAt": "2026-06-04T15:10:00"
}
```

### DELETE `/api/profiles/{profileId}`

인증: 필요

응답: `204 No Content`

## AI Consultation

### POST `/api/profiles/{profileId}/consultations`

인증: 필요

요청:

```json
{
  "question": "올해 커리어 방향을 어떻게 잡으면 좋을까?"
}
```

응답:

```json
{
  "id": 1,
  "userId": 1,
  "profileId": 1,
  "question": "올해 커리어 방향을 어떻게 잡으면 좋을까?",
  "answer": "[Mock AI 상담 답변] ...",
  "createdAt": "2026-06-04T15:00:00"
}
```

### GET `/api/profiles/{profileId}/consultations`

인증: 필요

응답:

```json
[
  {
    "id": 1,
    "userId": 1,
    "profileId": 1,
    "question": "올해 커리어 방향을 어떻게 잡으면 좋을까?",
    "answer": "[Mock AI 상담 답변] ...",
    "createdAt": "2026-06-04T15:00:00"
  }
]
```

## Health Check

### GET `/api/health`

인증: 불필요

응답:

```json
{
  "timestamp": "2026-06-04T15:00:00",
  "status": "UP"
}
```

## 예외 응답

### 401 Unauthorized

토큰이 없거나 유효하지 않을 때:

```json
{
  "status": 401,
  "message": "Unauthorized",
  "timestamp": "2026-06-04T15:00:00"
}
```

### 403 Forbidden

인증은 되었지만 권한이 부족할 때:

```json
{
  "status": 403,
  "message": "Forbidden",
  "timestamp": "2026-06-04T15:00:00"
}
```

### 404 Not Found

존재하지 않는 프로필이거나 타인 프로필 접근을 숨길 때:

```json
{
  "status": 404,
  "message": "Profile not found",
  "timestamp": "2026-06-04T15:00:00"
}
```

### 429 Too Many Requests

일일 AI 상담 요청 제한을 초과했을 때:

```json
{
  "status": 429,
  "message": "Daily AI consultation limit exceeded",
  "timestamp": "2026-06-04T15:00:00"
}
```

### 500 Internal Server Error

서버 내부 오류:

```json
{
  "status": 500,
  "message": "Internal server error",
  "timestamp": "2026-06-04T15:00:00"
}
```
