# AI 사주 기반 개인화 상담 서비스

회원가입한 사용자가 여러 사주 프로필을 등록하고, 선택한 프로필의 기본 분석 결과를 바탕으로 AI 상담을 진행하는 Spring Boot 백엔드 MVP입니다.

## 핵심 기능

- JWT 기반 회원가입/로그인
- 사용자별 다중 사주 프로필 생성, 목록, 상세, 삭제
- 사주 입력값 기반 기본 분석 결과 생성 및 DB 저장
- Redis 기반 동일 사주 입력 분석 결과 캐싱
- 프로필 정보, 분석 결과, 이전 상담 기록을 포함한 AI 상담
- Redis 기반 사용자별 일일 AI 상담 요청 제한
- local/dev/prod 환경 분리
- Docker Compose 기반 Spring Boot, MySQL, Redis 실행

## 기술 스택

- Java 21
- Spring Boot 3.3
- Spring Security, JWT
- Spring Data JPA, MySQL
- Spring Data Redis
- Gradle
- Docker Compose
- OpenAI API 연동 구조 및 Mock AI Client

## 실행 방법

```bash
gradle bootRun
```

local profile은 기본 활성화되어 있으며, 로컬 MySQL과 Redis가 필요합니다.

## Docker Compose 실행

```bash
docker compose up --build
```

실행 후 health check:

```bash
curl http://localhost:8080/api/health
```

## 환경 설명

- `application-local.yml`: Docker Compose 또는 로컬 MySQL/Redis 사용, AI Mock 기본
- `application-dev.yml`: 환경변수 기반 DB/Redis/OpenAI 설정, JPA validate
- `application-prod.yml`: 운영 환경변수 기반 설정, 민감정보 하드코딩 없음

주요 환경변수:

- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD`
- `JWT_SECRET`
- `AI_PROVIDER=mock|openai`
- `OPENAI_API_KEY`

## API 요약

- `POST /api/auth/signup`
- `POST /api/auth/login`
- `POST /api/profiles`
- `GET /api/profiles`
- `GET /api/profiles/{profileId}`
- `DELETE /api/profiles/{profileId}`
- `POST /api/profiles/{profileId}/consultations`
- `GET /api/profiles/{profileId}/consultations`
- `GET /api/health`

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
- 테스트 컨테이너 기반 통합 테스트
- AWS EC2 배포용 Nginx, HTTPS, CI/CD 구성
