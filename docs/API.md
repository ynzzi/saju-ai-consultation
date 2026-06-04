# API 명세

## Auth

### POST `/api/auth/signup`

인증: 불필요

```json
{
  "email": "user@example.com",
  "password": "password123",
  "nickname": "yunji"
}
```

```json
{
  "accessToken": "jwt-token",
  "tokenType": "Bearer"
}
```

### POST `/api/auth/login`

인증: 불필요

```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

## Saju Profile

### POST `/api/profiles`

인증: 필요

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

### GET `/api/profiles`

인증: 필요

### GET `/api/profiles/{profileId}`

인증: 필요

### DELETE `/api/profiles/{profileId}`

인증: 필요

## AI Consultation

### POST `/api/profiles/{profileId}/consultations`

인증: 필요

```json
{
  "question": "올해 커리어 방향을 어떻게 잡으면 좋을까?"
}
```

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

## Health Check

### GET `/api/health`

인증: 불필요

## 공통 에러 응답

```json
{
  "status": 404,
  "message": "Profile not found",
  "timestamp": "2026-06-04T15:00:00"
}
```
