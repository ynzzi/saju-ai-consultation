# Troubleshooting

## MySQL 연결 오류

- `docker compose ps`로 `saju-mysql` 상태를 확인합니다.
- IntelliJ local 실행 기준 기본 접속 주소는 `localhost:3307`입니다.
- Docker Compose app 컨테이너 기준 접속 주소는 `mysql:3306`입니다.
- `Communications link failure`가 발생하면 MySQL 컨테이너 health check가 끝난 뒤 앱을 다시 실행합니다.

## 3306 포트 충돌

Mac 로컬에 MariaDB 또는 MySQL이 설치되어 있으면 `mysqld`가 이미 3306을 점유하는 경우가 많습니다.

확인:

```bash
lsof -i :3306
```

Docker Compose에서는 호스트 포트를 `3307:3306`으로 매핑해 충돌을 피합니다.

```yaml
mysql:
  ports:
    - "3307:3306"
```

이 경우 IntelliJ local 실행의 datasource URL도 `localhost:3307`을 사용해야 합니다.

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3307/saju_app?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
```

현재 `application-local.yml`은 기본값을 `MYSQL_PORT:3307`로 둡니다. 단, Docker Compose의 app 컨테이너는 환경변수 `MYSQL_HOST=mysql`, `MYSQL_PORT=3306`을 주입받기 때문에 컨테이너 내부에서는 그대로 `mysql:3306`에 연결됩니다.

## phpMyAdmin 또는 다른 앱이 8080을 점유하는 경우

phpMyAdmin, Tomcat, 다른 Spring Boot 앱이 8080을 사용 중이면 Spring Boot app과 충돌합니다.

확인:

```bash
lsof -i :8080
```

Docker Compose 전체 실행 시 호스트 포트를 바꿉니다.

```bash
APP_PORT=8081 docker compose up --build
```

이후 health check도 바뀐 포트를 사용합니다.

```bash
curl http://localhost:8081/api/health
```

IntelliJ local 실행에서는 `application.yml` 또는 Run Configuration의 `server.port`를 변경합니다.

```bash
-Dserver.port=8081
```

## Docker Compose app 컨테이너와 IntelliJ local 실행의 DB host 차이

가장 자주 헷갈리는 부분입니다.

- IntelliJ local 실행: 앱이 Mac 호스트에서 실행되므로 MySQL host는 `localhost`, port는 `3307`
- Docker Compose app 실행: 앱이 Docker 네트워크 안에서 실행되므로 MySQL host는 서비스명 `mysql`, port는 `3306`

Compose의 app 환경변수:

```yaml
MYSQL_HOST: mysql
MYSQL_PORT: 3306
REDIS_HOST: redis
REDIS_PORT: 6379
```

IntelliJ local 기본값:

```yaml
MYSQL_HOST: localhost
MYSQL_PORT: 3307
REDIS_HOST: localhost
REDIS_PORT: 6379
```

## Redis 연결 오류

- `docker compose ps`로 `saju-redis` 상태를 확인합니다.
- IntelliJ local 실행 시 `REDIS_HOST=localhost`를 사용합니다.
- Docker Compose app 컨테이너에서는 `REDIS_HOST=redis`를 사용합니다.
- Redis가 꺼져 있으면 프로필 분석 캐시와 AI 요청 제한이 동작하지 않습니다.

## Redis Repository 스캔 INFO 로그

이 프로젝트는 Redis Repository를 사용하지 않고 `StringRedisTemplate`만 사용합니다.

`application.yml`에서 Redis Repository 스캔을 끕니다.

```yaml
spring:
  data:
    redis:
      repositories:
        enabled: false
```

JPA Repository는 계속 Spring Data JPA가 스캔하고, Redis는 캐시와 요청 제한을 위해 Template 방식으로만 사용합니다.

## JWT 인증 오류

- 보호 API에는 `Authorization: Bearer {accessToken}` 헤더가 필요합니다.
- `JWT_SECRET`이 바뀌면 기존 토큰은 검증되지 않습니다.
- 만료 시간은 `JWT_ACCESS_TOKEN_VALIDITY_MS`로 조정합니다.
- 응답이 `401 Unauthorized`라면 토큰 누락, 만료, 형식 오류를 먼저 확인합니다.

## Docker Compose 실행 오류

- 8080, 3307, 6379 포트가 이미 사용 중인지 확인합니다.
- 기존 볼륨 데이터와 계정 정보가 충돌하면 compose 프로젝트 볼륨을 정리한 뒤 다시 실행합니다.
- app 빌드가 실패하면 먼저 `docker compose build app`으로 빌드 로그를 확인합니다.

## 환경변수 누락 오류

- dev/prod profile은 `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `REDIS_HOST`, `JWT_SECRET`이 필요합니다.
- `APP_AI_PROVIDER=openai`를 사용할 때는 `OPENAI_API_KEY`가 필요합니다.
- 민감정보는 설정 파일에 직접 쓰지 말고 EC2 환경변수, Parameter Store, Secret Manager 등을 사용합니다.

## OPENAI_API_KEY 누락

local 기본값은 `APP_AI_PROVIDER=mock`이므로 API Key 없이도 동작합니다.

`APP_AI_PROVIDER=openai`인데 `OPENAI_API_KEY`가 비어 있으면 앱은 기동되지만 AI 상담 요청 시 다음 사용자 메시지를 반환합니다.

```text
AI 답변 생성 중 일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.
```

서버 로그에는 `OpenAI provider is enabled but OPENAI_API_KEY is missing.` 경고가 남습니다.

## OpenAI API 호출 실패

네트워크 오류, API 응답 오류, 타임아웃, 모델명 오류가 발생하면 사용자에게는 내부 상세 원인을 노출하지 않습니다.

확인할 항목:

- `OPENAI_API_KEY`가 유효한지 확인합니다.
- `OPENAI_MODEL`이 사용 가능한 모델명인지 확인합니다.
- 서버에서 외부 네트워크 호출이 가능한지 확인합니다.
- OpenAI API 사용량/한도 상태를 확인합니다.

## provider 설정 오류

사용 가능한 값은 다음 두 가지입니다.

```text
APP_AI_PROVIDER=mock
APP_AI_PROVIDER=openai
```

다른 값을 설정하면 `AiClient` Bean이 생성되지 않아 애플리케이션 기동이 실패할 수 있습니다. local 개발에서는 `mock`, dev/prod 실제 연동에서는 `openai`를 사용합니다.

## Docker Compose에서 OpenAI 환경변수가 전달되지 않는 경우

Compose 실행 시 환경변수를 같은 명령에 함께 전달합니다.

```bash
OPENAI_API_KEY={your_api_key} APP_AI_PROVIDER=openai docker compose up --build
```

설정이 반영됐는지 확인하려면 `docker compose config`로 app 서비스의 environment 항목을 확인합니다. API Key는 터미널 출력이나 문서에 남기지 않도록 주의하세요.

## 운영 환경 ddl-auto 주의

운영 환경에서 `ddl-auto=create` 또는 `ddl-auto=update`는 데이터 손상 위험이 있습니다.

현재 prod profile은 다음처럼 `validate`를 사용합니다.

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
```

운영 배포에서는 Flyway 또는 Liquibase를 도입해 스키마 변경을 명시적으로 관리하는 방향을 권장합니다.

## 만세력 계산 결과가 전문 만세력과 다르게 보이는 경우

현재 구현은 포트폴리오 MVP 범위의 기본 만세력 계산입니다. 검증된 음양력 변환 라이브러리나 한국천문연구원 API를 사용하지 않으므로, 음력 입력은 별도 양력 변환 없이 입력 날짜를 계산 기준일로 사용합니다. 윤달 여부는 저장, 응답, Redis 캐시 키에는 반영하지만 실제 날짜 변환 보정에는 사용하지 않습니다.

정확한 전문 계산이 필요하면 `ManseCalendarService`를 외부 API 또는 검증된 라이브러리 기반 구현으로 교체해야 합니다. 상세 기준은 [MANSE_CALENDAR.md](MANSE_CALENDAR.md)를 참고하세요.

## Redis 캐시가 이전 분석 결과를 보여주는 경우

만세력 계산 적용 이후 분석 캐시 키는 `saju:analysis:v3:manse`로 변경되었습니다. 이전 `saju:profile:v2` 캐시는 새 분석 로직에서 사용하지 않습니다. Redis에 오래된 키가 남아 있어도 신규 분석에는 영향을 주지 않습니다.
