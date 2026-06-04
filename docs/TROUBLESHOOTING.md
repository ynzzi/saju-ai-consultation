# Troubleshooting

## MySQL 연결 오류

- `docker compose ps`로 `saju-mysql` 상태를 확인합니다.
- local 실행 시 `MYSQL_HOST`, `MYSQL_PORT`, `MYSQL_DATABASE`, `MYSQL_USER`, `MYSQL_PASSWORD` 값이 `application-local.yml`과 맞는지 확인합니다.
- `Communications link failure`가 발생하면 MySQL 컨테이너 health check가 끝난 뒤 앱을 다시 실행합니다.

## Redis 연결 오류

- `docker compose ps`로 `saju-redis` 상태를 확인합니다.
- local 실행 시 `REDIS_HOST=localhost`, Docker Compose 실행 시 app 컨테이너에서는 `REDIS_HOST=redis`를 사용합니다.
- Redis가 꺼져 있으면 프로필 분석 캐시와 AI 요청 제한이 동작하지 않습니다.

## JWT 인증 오류

- 보호 API에는 `Authorization: Bearer {accessToken}` 헤더가 필요합니다.
- `JWT_SECRET`이 바뀌면 기존 토큰은 검증되지 않습니다.
- 만료 시간은 `JWT_ACCESS_TOKEN_VALIDITY_MS`로 조정합니다.

## Docker Compose 실행 오류

- 8080, 3306, 6379 포트가 이미 사용 중인지 확인합니다.
- 기존 볼륨 데이터와 계정 정보가 충돌하면 compose 프로젝트 볼륨을 정리한 뒤 다시 실행합니다.
- app 빌드가 실패하면 먼저 `./gradlew clean build`로 로컬 컴파일 오류를 확인합니다.

## 환경변수 누락 오류

- dev/prod profile은 `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `REDIS_HOST`, `JWT_SECRET`이 필요합니다.
- `AI_PROVIDER=openai`를 사용할 때는 `OPENAI_API_KEY`가 필요합니다.
- 민감정보는 설정 파일에 직접 쓰지 말고 EC2 환경변수, Parameter Store, Secret Manager 등을 사용합니다.
