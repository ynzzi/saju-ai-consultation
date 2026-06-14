# MVP 만세력 계산 기준

이 프로젝트는 기존 생월/출생시간 기반 규칙형 분석을 개선해 사주팔자 핵심 값을 함께 계산합니다. 다만 전문 감정 서비스 수준의 완전한 만세력 구현은 아니며, 포트폴리오 MVP 범위에서 계산 기준과 한계를 명시합니다.

## 적용 범위

- 년주, 월주, 일주, 시주 계산
- 천간/지지 표시
- 오행 분포 계산
- 음양 분포 계산
- AI 상담 프롬프트에 사주팔자와 계산 기준 포함
- Redis 분석 캐시 키 버전 `saju:analysis:v3:manse`

## 계산 기준

- 시간대: `Asia/Seoul` 기준 입력으로 간주합니다.
- 지원 날짜 범위: `1900-01-01`부터 `2099-12-31`까지입니다.
- 양력 입력: 입력 날짜를 그대로 계산 기준일로 사용합니다.
- 음력 입력: 현재 버전은 검증된 음양력 변환 라이브러리/API를 사용하지 않습니다. 따라서 입력된 음력 날짜를 별도 변환 없이 계산 기준일로 사용하며, 화면과 API 응답에 경고 문구를 포함합니다.
- 윤달: 음력 입력에서만 허용합니다. 윤달 여부는 저장/캐시/응답에는 반영하지만, 현재 MVP 계산에서는 별도 양력 변환 보정에 사용하지 않습니다.
- 년주: 매년 2월 4일을 입춘 기준일로 단순 적용합니다. 실제 입춘 시각은 연도별로 달라질 수 있습니다.
- 월주: 24절기 전체 시각 계산 대신 절기 경계일 근사값을 사용합니다.
- 일주: `1900-01-31`을 갑진일로 두고 60갑자 순환을 계산합니다.
- 시주: 전통적인 2시간 단위 지지 구간과 일간별 시간 천간 표를 사용합니다.
- 자시 처리: `23:00~23:59`는 다음 날로 넘기지 않고 같은 날짜의 자시로 계산합니다. 야자시/조자시 구분은 적용하지 않습니다.

## 정확도 한계

- 실제 음력-양력 변환을 수행하지 않습니다.
- 실제 24절기 진입 시각을 천문 계산하거나 외부 API로 검증하지 않습니다.
- 입춘 기준도 날짜 단위로만 처리하며, 시각 단위 경계는 반영하지 않습니다.
- 지역별 태양시 보정은 적용하지 않습니다.
- 전문 사주 해석의 기준 차이가 있는 영역은 이 프로젝트에서 선택한 MVP 기준으로만 처리합니다.

## 향후 개선 방향

- 한국천문연구원 또는 공공데이터포털 API를 통한 음양력/절기 검증
- API Key 환경변수 주입 및 장애 시 fallback 정책 정교화
- 윤달을 포함한 실제 음력 날짜 변환
- 절기 시각 기반 월주 계산
- 야자시/조자시 옵션화
- 계산 결과에 대한 기준일/변환일 별도 저장

## DB 변경 사항

새 프로필부터 만세력 계산 결과를 저장하기 위해 `saju_profiles`에 nullable 컬럼을 추가합니다. local profile은 `ddl-auto=update`라 자동 반영될 수 있지만, prod profile은 `ddl-auto=validate`이므로 운영 DB에는 배포 전에 직접 반영해야 합니다.

```sql
ALTER TABLE saju_profiles ADD COLUMN leap_month bit;
ALTER TABLE saju_profiles ADD COLUMN year_pillar varchar(20);
ALTER TABLE saju_profiles ADD COLUMN month_pillar varchar(20);
ALTER TABLE saju_profiles ADD COLUMN day_pillar varchar(20);
ALTER TABLE saju_profiles ADD COLUMN hour_pillar varchar(20);
ALTER TABLE saju_profiles ADD COLUMN five_elements_summary varchar(1000);
ALTER TABLE saju_profiles ADD COLUMN yin_yang_summary varchar(1000);
ALTER TABLE saju_profiles ADD COLUMN calculation_standard varchar(1000);
ALTER TABLE saju_profiles ADD COLUMN calculation_warning varchar(1000);
ALTER TABLE saju_profiles ADD COLUMN manse_calendar_version varchar(60);
```

기존 데이터는 새 컬럼이 `NULL`이어도 조회 가능하며, `분석 다시 생성`을 실행하면 새 계산 결과가 채워집니다.
