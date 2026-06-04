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
- `AI_PROVIDER=openai`를 사용할 때는 `OPENAI_API_KEY`가 필요합니다.
- 민감정보는 설정 파일에 직접 쓰지 말고 EC2 환경변수, Parameter Store, Secret Manager 등을 사용합니다.

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
