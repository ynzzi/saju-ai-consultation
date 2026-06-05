# AI 사주 기반 개인화 상담 서비스

회원가입한 사용자가 여러 사주 프로필을 등록하고, 선택한 프로필의 기본 분석 결과를 바탕으로 AI 상담을 진행하는 Spring Boot 백엔드 MVP입니다.

## 핵심 기능

- JWT 기반 회원가입/로그인
- 사용자별 다중 사주 프로필 생성, 목록, 상세, 삭제
- 생월, 출생시간, 양력/음력 기반 MVP 규칙형 분석 결과 생성 및 DB 저장
- Redis 기반 동일 사주 입력 분석 결과 캐싱
- 프로필 정보, 분석 결과, 이전 상담 기록을 포함한 AI 상담
- Redis 기반 사용자별 일일 AI 상담 요청 제한
- 프로필 상세 화면에서 기존 프로필 분석 결과 재생성
- local/dev/prod 환경 분리
- Docker Compose 기반 app, MySQL, Redis 실행

## 사주 분석 로직

현재 분석은 완전한 만세력 계산이 아니라 MVP용 rule-based 분석입니다.

- 생월을 기준으로 봄, 여름, 가을, 겨울의 계절 성향을 판단합니다.
- 출생시간을 기준으로 새벽, 오전, 오후, 저녁/밤의 시간대 에너지를 판단합니다.
- 양력/음력 선택값에 따라 현실적 흐름 또는 전통적/관계적 흐름의 문구를 조정합니다.
- 성별은 현재 단계에서 차별적 해석에 사용하지 않고 프로필 정보로만 유지합니다.
- 향후 실제 만세력, 오행, 십성, 대운/세운 계산 모듈로 교체할 수 있도록 `SajuAnalysisService` 인터페이스 기반으로 분리되어 있습니다.

## 기술 스택

- Java 21
- Spring Boot 3.3
- Spring Security, JWT
- Spring Data JPA, MySQL
- Spring Data Redis, RedisTemplate
- Gradle
- Docker Compose
- OpenAI API 연동 구조 및 Mock AI Client

## AI Provider 설정

local 환경은 기본적으로 `MockAiClient`를 사용합니다. API Key 없이도 회원가입, 프로필 생성, AI 상담 화면 흐름을 개발하고 테스트할 수 있습니다.

사용 가능한 환경변수:

- `APP_AI_PROVIDER`: `mock` 또는 `openai`
- `OPENAI_MODEL`: 사용할 OpenAI 모델명, 기본값 `gpt-5.5`
- `OPENAI_API_KEY`: OpenAI API Key

실제 OpenAI 연동으로 실행하려면 환경변수로 값을 주입합니다. API Key는 절대 Git에 커밋하지 마세요.

```bash
OPENAI_API_KEY={your_api_key} APP_AI_PROVIDER=openai docker compose up --build
```

Mock으로 명시 실행:

```bash
APP_AI_PROVIDER=mock docker compose up --build
```

정책:

- `APP_AI_PROVIDER=mock`: `MockAiClient` 사용
- `APP_AI_PROVIDER=openai`: `OpenAiClient` 사용
- `openai` provider에서 `OPENAI_API_KEY`가 비어 있으면 앱은 기동되지만 상담 요청 시 사용자에게 `AI 답변 생성 중 일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.` 메시지를 반환합니다.
- prod에서 조용히 Mock으로 fallback하지 않습니다. 운영에서는 `APP_AI_PROVIDER=openai`와 `OPENAI_API_KEY`를 명확히 설정해야 합니다.

## 실행 방식

### 1. IntelliJ local 실행

IntelliJ에서 Spring Boot Run Configuration을 만들고 active profile을 `local`로 설정합니다.

local profile 기본 연결값:

- MySQL: `localhost:3307`
- Redis: `localhost:6379`
- Database: `saju_app`
- User: `saju_user`
- Password: `saju_password`
- AI: `mock`

먼저 MySQL과 Redis만 Docker로 실행합니다.

```bash
docker compose up -d mysql redis
```

그 다음 IntelliJ에서 `SajuAiConsultationApplication`을 실행합니다.

### 2. Docker Compose 전체 실행

app, mysql, redis를 한 번에 실행합니다.

```bash
docker compose up --build
```

Compose 실행 시 app 컨테이너는 Docker 네트워크 내부 주소를 사용합니다.

- MySQL host: `mysql`
- MySQL port: `3306`
- Redis host: `redis`
- Redis port: `6379`

호스트 Mac에서는 MySQL이 `localhost:3307`로 노출됩니다. 3306은 로컬 MariaDB나 mysqld와 충돌하기 쉬워 기본 Compose 포트를 `3307:3306`으로 둡니다.

phpMyAdmin 등으로 8080이 이미 사용 중이면 app 포트를 바꿔 실행할 수 있습니다.

```bash
APP_PORT=8081 docker compose up --build
```

### 3. EC2 prod 배포 실행

운영 배포는 `.env` 파일을 서버에 직접 만들고 prod compose를 사용합니다. 실제 `.env`는 Git에 커밋하지 않고, 저장소에는 [.env.example](.env.example)만 제공합니다.

```bash
cp .env.example .env
docker compose -f docker-compose.prod.yml --env-file .env up --build -d
```

자세한 절차는 [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md)를 참고하세요.

## Health Check

```bash
curl http://localhost:8080/api/health
```

응답 예시:

```json
{
  "timestamp": "2026-06-04T15:00:00",
  "status": "UP"
}
```

## 화면 테스트 흐름

- `GET /view/signup`: 회원가입
- `GET /view/login`: 로그인
- `GET /view/profiles`: 프로필 목록
- `GET /view/profiles/new`: 프로필 등록
- `GET /view/profiles/{profileId}`: 프로필 상세
- `GET /view/profiles/{profileId}/consultations`: AI 상담

브라우저 확인 순서:

1. `/view/signup`에서 회원가입
2. `/view/profiles/new`에서 프로필 생성
3. `/view/profiles/{profileId}`에서 분석 결과 확인
4. `분석 다시 생성` 버튼으로 최신 분석 결과 갱신
5. 추천 질문 버튼을 클릭해 AI 상담 화면으로 이동
6. 질문 입력창에 추천 질문이 자동 입력되는지 확인
7. `질문하기` 클릭 후 로딩 말풍선과 Mock AI 답변 확인
8. 새로고침 후 상담 기록 유지 확인

## API 테스트 순서

### 1. 회원가입

```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123","nickname":"yunji"}'
```

응답의 `accessToken`을 저장합니다.

```bash
TOKEN="발급받은_accessToken"
```

### 2. 로그인

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123"}'
```

### 3. 사주 프로필 생성

`gender`는 MVP 기준 `MALE`, `FEMALE`만 사용합니다.

```bash
curl -X POST http://localhost:8080/api/profiles \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "profileName": "내 프로필",
    "birthDate": "1998-03-15",
    "birthTime": "09:30",
    "calendarType": "SOLAR",
    "gender": "FEMALE",
    "birthPlace": "Seoul"
  }'
```

응답의 `id`를 저장합니다.

```bash
PROFILE_ID=1
```

### 4. 프로필 목록/상세 조회

```bash
curl http://localhost:8080/api/profiles \
  -H "Authorization: Bearer $TOKEN"
```

```bash
curl http://localhost:8080/api/profiles/$PROFILE_ID \
  -H "Authorization: Bearer $TOKEN"
```

분석 로직이 개선된 경우 기존 프로필을 삭제하지 않고 최신 분석 결과로 갱신할 수 있습니다. 프로필 기본 정보와 상담 기록은 유지됩니다.

```bash
curl -X POST http://localhost:8080/api/profiles/$PROFILE_ID/reanalyze \
  -H "Authorization: Bearer $TOKEN"
```

### 5. AI 상담 생성

```bash
curl -X POST http://localhost:8080/api/profiles/$PROFILE_ID/consultations \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"question":"올해 커리어 방향을 어떻게 잡으면 좋을까?"}'
```

### 6. 상담 기록 조회

```bash
curl http://localhost:8080/api/profiles/$PROFILE_ID/consultations \
  -H "Authorization: Bearer $TOKEN"
```

브라우저 상담 화면에서는 같은 API를 호출합니다.

```text
/view/profiles/{profileId}/consultations
```

## Redis 요청 제한 테스트

local 환경의 일일 AI 상담 제한은 기본 20회입니다.

```bash
for i in {1..21}; do
  curl -s -X POST http://localhost:8080/api/profiles/$PROFILE_ID/consultations \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d "{\"question\":\"요청 제한 테스트 $i\"}"
  echo
done
```

20회를 초과하면 다음과 같은 응답을 받습니다.

```json
{
  "status": 429,
  "message": "Daily AI consultation limit exceeded",
  "timestamp": "2026-06-04T15:00:00"
}
```

브라우저 상담 화면에서는 429 응답 시 질문 입력이 비활성화되고 다음 메시지가 표시됩니다.

```text
오늘의 AI 상담 요청 횟수를 초과했습니다. 내일 다시 이용해주세요.
```

Redis key 형식:

```text
ai:limit:{userId}:{yyyyMMdd}
```

## 다른 사용자 프로필 접근 제한 테스트

두 번째 사용자를 만들고 토큰을 발급합니다.

```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"email":"other@example.com","password":"password123","nickname":"other"}'
```

```bash
OTHER_TOKEN="두번째_사용자_accessToken"
```

첫 번째 사용자의 `PROFILE_ID`를 두 번째 사용자 토큰으로 조회합니다.

```bash
curl http://localhost:8080/api/profiles/$PROFILE_ID \
  -H "Authorization: Bearer $OTHER_TOKEN"
```

현재 구현은 타인 프로필을 노출하지 않기 위해 `404 Profile not found`로 응답합니다.

## 환경 설명

- `application-local.yml`: IntelliJ local 실행 기준, MySQL `localhost:3307`, Redis `localhost:6379`
- `application-dev.yml`: 환경변수 기반 DB/Redis/OpenAI 설정, JPA `validate`
- `application-prod.yml`: 운영 환경변수 기반 설정, JPA `validate`, 민감정보 하드코딩 없음

주요 환경변수:

- `MYSQL_HOST`, `MYSQL_PORT`, `MYSQL_DATABASE`, `MYSQL_USER`, `MYSQL_PASSWORD`
- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD`
- `JWT_SECRET`
- `APP_AI_PROVIDER=mock|openai`
- `OPENAI_MODEL`
- `OPENAI_API_KEY`

## 프로젝트 구조

```text
com.portfolio.saju
├── auth
├── common
├── config
├── consultation
├── health
├── profile
├── security
└── user
```

## 향후 개선 사항

- 실제 만세력, 대운, 세운 계산 모듈 도입
- Refresh Token 및 토큰 재발급
- 상담방 단위 세션 모델링
- OpenAI API 응답 스트리밍
- Flyway 또는 Liquibase 기반 마이그레이션
- 테스트 컨테이너 기반 통합 테스트
- AWS EC2 배포용 Nginx, HTTPS, CI/CD 구성
