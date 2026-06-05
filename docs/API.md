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

요청 필드 허용값:

- `calendarType`: `SOLAR`, `LUNAR`
- `gender`: `MALE`, `FEMALE`

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
  "birthTime": "09:30:00",
  "calendarType": "SOLAR",
  "gender": "FEMALE",
  "birthPlace": "Seoul"
}
```

응답:

```json
{
  "id": 1,
  "profileName": "내 프로필",
  "birthDate": "1998-03-15",
  "birthTime": "09:30:00",
  "calendarType": "SOLAR",
  "gender": "FEMALE",
  "birthPlace": "Seoul",
  "analysisSummary": "양력 기준 입력으로 볼 때...",
  "elementSummary": "현재 MVP 분석에서는...",
  "strengths": ["상황을 빠르게 정리하는 힘"],
  "cautions": ["혼자 감당하려는 습관"],
  "recommendedQuestions": ["올해 일과 관계에서 가장 신경 써야 할 부분은?"],
  "createdAt": "2026-06-04T15:00:00",
  "updatedAt": "2026-06-04T15:00:00"
}
```

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
    "birthPlace": "Seoul",
    "analysisSummary": "양력 기준 입력으로 볼 때...",
    "elementSummary": "현재 MVP 분석에서는...",
    "strengths": ["상황을 빠르게 정리하는 힘"],
    "cautions": ["혼자 감당하려는 습관"],
    "recommendedQuestions": ["올해 일과 관계에서 가장 신경 써야 할 부분은?"],
    "createdAt": "2026-06-04T15:00:00",
    "updatedAt": "2026-06-04T15:00:00"
  }
]
```

### GET `/api/profiles/{profileId}`

인증: 필요

응답: `POST /api/profiles` 응답과 동일한 구조

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
