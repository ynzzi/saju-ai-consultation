# EC2 Deployment

## 개요

이 문서는 AWS EC2에서 Docker Compose로 AI 사주 상담 서비스를 실행하기 위한 배포 절차입니다.

운영 실행 명령:

```bash
docker compose -f docker-compose.prod.yml --env-file .env up --build -d
```

prod compose는 `app`, `mysql`, `redis`를 같은 Docker 네트워크에서 실행합니다. 외부에는 기본적으로 app의 8080 포트만 노출하고, MySQL과 Redis 포트는 외부에 노출하지 않습니다.

## 사전 준비

- EC2 인스턴스
- Docker
- Docker Compose
- Git
- GitHub 저장소 접근 권한

## 보안 그룹

테스트용 최소 인바운드 포트:

- `22`: SSH
- `8080`: HTTP 테스트용

실제 서비스에서는 8080을 직접 노출하기보다 Nginx, HTTPS, 도메인을 붙이는 구성을 권장합니다.

## 서버 접속

```bash
ssh -i {key-pair.pem} ec2-user@{ec2-public-ip}
```

Ubuntu AMI라면 사용자명이 `ubuntu`일 수 있습니다.

## 저장소 Clone

```bash
git clone {github-repository-url}
cd {repository-directory}
```

## .env 생성

`.env.example`을 복사해 `.env`를 만듭니다.

```bash
cp .env.example .env
```

`.env`의 값은 서버에서 직접 수정합니다.

```env
DB_NAME=saju_app
DB_USERNAME=saju_user
DB_PASSWORD=change-me
MYSQL_ROOT_PASSWORD=change-me-root
JWT_SECRET=change-me-to-long-random-secret
APP_AI_PROVIDER=mock
OPENAI_MODEL=gpt-5.5
OPENAI_API_KEY=
JPA_DDL_AUTO=validate
APP_PORT=8080
```

주의:

- `.env`는 절대 Git에 커밋하지 않습니다.
- `JWT_SECRET`은 충분히 긴 랜덤 문자열로 바꿉니다.
- `APP_AI_PROVIDER=mock`이면 `OPENAI_API_KEY` 없이 동작합니다.
- `APP_AI_PROVIDER=openai`이면 `OPENAI_API_KEY`가 필요합니다.

## DB 스키마 주의

`application-prod.yml`은 기본적으로 `JPA_DDL_AUTO=validate`를 사용합니다. 운영에서 Hibernate가 테이블을 자동 생성/변경하지 않도록 하는 안전한 기본값입니다.

단, MVP 첫 배포에서 MySQL이 완전히 비어 있으면 `validate`는 테이블이 없어서 앱 기동에 실패할 수 있습니다. 선택지는 두 가지입니다.

- 권장: Flyway 또는 Liquibase로 스키마 마이그레이션을 준비한 뒤 `validate` 유지
- MVP 임시 방식: 최초 1회만 `.env`에서 `JPA_DDL_AUTO=update`로 실행해 테이블을 만든 뒤, 다시 `validate`로 되돌림

운영 데이터가 생긴 이후에는 `create`를 절대 사용하지 마세요.

## 실행

```bash
docker compose -f docker-compose.prod.yml --env-file .env up --build -d
```

상태 확인:

```bash
docker compose -f docker-compose.prod.yml --env-file .env ps
```

## Health Check

```bash
curl http://localhost:8080/api/health
```

EC2 외부에서 확인:

```bash
curl http://{ec2-public-ip}:8080/api/health
```

정상 응답:

```json
{
  "status": "UP",
  "timestamp": "2026-06-05T12:00:00"
}
```

## 로그 확인

전체 로그:

```bash
docker compose -f docker-compose.prod.yml --env-file .env logs -f
```

app 로그:

```bash
docker compose -f docker-compose.prod.yml --env-file .env logs -f app
```

## 재시작

```bash
docker compose -f docker-compose.prod.yml --env-file .env restart app
```

전체 재배포:

```bash
git pull
docker compose -f docker-compose.prod.yml --env-file .env up --build -d
```

## 중지

```bash
docker compose -f docker-compose.prod.yml --env-file .env down
```

MySQL named volume은 유지됩니다. 볼륨 삭제가 필요한 경우에만 신중하게 별도 삭제합니다.

## 자주 나는 오류

### 8080 포트 충돌

확인:

```bash
sudo lsof -i :8080
```

`.env`에서 `APP_PORT`를 변경합니다.

```env
APP_PORT=8081
```

### DB 연결 실패

- `.env`의 `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`, `MYSQL_ROOT_PASSWORD`를 확인합니다.
- app 컨테이너는 Docker 네트워크 내부에서 `mysql:3306`으로 연결합니다.
- prod compose에서 MySQL 포트는 외부에 노출하지 않습니다.

### Redis 연결 실패

- app 컨테이너는 Docker 네트워크 내부에서 `redis:6379`로 연결합니다.
- prod compose에서 Redis 포트는 외부에 노출하지 않습니다.

### .env 누락

다음 파일이 있어야 합니다.

```bash
ls -al .env
```

없다면:

```bash
cp .env.example .env
```

### JWT_SECRET 누락

`JWT_SECRET`이 비어 있거나 너무 짧으면 JWT 서명에 문제가 날 수 있습니다. `.env`에서 충분히 긴 랜덤 문자열로 설정합니다.

### OPENAI_API_KEY 누락

`APP_AI_PROVIDER=mock`이면 누락되어도 됩니다.

`APP_AI_PROVIDER=openai`에서 `OPENAI_API_KEY`가 비어 있으면 AI 상담 요청 시 사용자에게 일시 오류 메시지가 반환되고, 서버 로그에 원인이 남습니다.

## 운영 주의사항

- `.env`는 절대 커밋하지 않습니다.
- MySQL/Redis 포트를 외부에 노출하지 않습니다.
- 실제 서비스에서는 HTTPS, Nginx, 도메인, 보안 그룹 제한을 적용합니다.
- DB 백업 정책을 별도로 마련합니다.
- 운영에서는 `JPA_DDL_AUTO=validate`를 기본으로 유지하고, 스키마 변경은 마이그레이션 도구로 관리하는 방향을 권장합니다.
