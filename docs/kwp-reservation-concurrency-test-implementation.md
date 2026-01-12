# 예약 동시성 부하 테스트/대기열 요구사항 정리 (초안)

## 목적
- 예약 생성 API의 동시성 부하를 재현하고, 제한 인원 초과 시 실패/대기 처리 UX를 검증한다.
- 현행 아키텍처(Nginx 공개 엔드포인트 + WAS + MySQL/Redis) 기준으로 테스트 경로를 정의한다.

## 테스트 목표
- 대상 API: `POST /api/reservations`
- 동시 접속자: 100명
- 요청률: 100 RPS
- 예약 성공은 제한된 인원까지만 허용, 나머지는 실패 또는 대기 등록

## 테스트 경로
- 실행 위치: bastion에서 부하 도구 실행
- 타깃: Nginx 공개 엔드포인트
- 이유: 실제 사용자 경로(게이트웨이 포함)를 재현하여 네트워크/프록시 병목까지 확인

## 테스트 시나리오 설계
### 1) 사용자 인증
- 예약 생성은 인증 필요
- 부하 도구에서 사용자별 토큰을 사전에 발급하거나 세션 쿠키를 분배해야 한다.
- 동시 사용자 수만큼 토큰/세션을 준비한다.

### 2) 슬롯 고정
- 동일한 `restaurantId/slotDate/slotTime`으로 요청을 집중
- 동일 슬롯에서 좌석 수(또는 파티 사이즈 합) 제한을 초과하는 상황을 유발

### 3) 요청 페이로드 고정
- 최소 필수 필드만 포함하고, 동일 페이로드로 반복 요청
- 실패 응답의 형태(코드/메시지)를 확인한다.

### 4) 성공/실패 기준
- 성공: 예약 생성 및 예약 ID 발급
- 실패: 좌석/중복 제한에 따른 에러 응답(예: 409/429)
- 대기: 대기열 등록 응답(향후 구현)

## 부하 도구 후보
- k6: 선언형 스크립트 + RPS 제어가 쉬움
- Locust: 사용자 행위 시뮬레이션에 유리

## k6 예약 부하 스크립트
- 경로: `scripts/k6_reservation_loadtest.js`
- 기본값: 100 RPS, 100 VU, 1분

```bash
docker run --rm -i grafana/k6 run - \
  -e BASE_URL=http://10.0.2.6:8080 \
  -e EMAIL_PREFIX=loadtest.user \
  -e EMAIL_DOMAIN=example.com \
  -e PASSWORD='Passw0rd!123' \
  -e RESTAURANT_ID=4 \
  -e SLOT_DATE=2026-01-16 \
  -e SLOT_TIME=12:00 \
  -e PARTY_SIZE=4 \
  -e RESERVATION_TYPE=RESERVATION_DEPOSIT \
  -e LOAD_RATE=100 \
  -e LOAD_VUS=100 \
  -e LOAD_MAX_VUS=200 \
  -e LOAD_DURATION=1m \
  < scripts/k6_reservation_loadtest.js
```

### 파라미터 설명
- `BASE_URL`: 예약/로그인 API 베이스 URL
- `EMAIL_PREFIX`: 테스트 계정 이메일 prefix (`loadtest.user0001`의 앞부분)
- `EMAIL_DOMAIN`: 테스트 계정 이메일 도메인
- `PASSWORD`: 테스트 계정 비밀번호(BCrypt 해시 전 원문)
- `RESTAURANT_ID`: 예약 대상 식당 ID
- `SLOT_DATE`: 예약 날짜 (YYYY-MM-DD)
- `SLOT_TIME`: 예약 시간 (HH:mm)
- `PARTY_SIZE`: 예약 인원
- `RESERVATION_TYPE`: 예약 타입 (`RESERVATION_DEPOSIT` 또는 `PREORDER_PREPAY`)
- `LOAD_RATE`: 초당 요청 수(RPS)
- `LOAD_VUS`: 시작 VU 수(시작 시점에 확보해두는 가상 사용자 풀 크기, 로그인 선행을 의미하지 않음)
- `LOAD_MAX_VUS`: 최대 VU 수(응답 지연 시 증가)
- `LOAD_DURATION`: 부하 지속 시간 (예: `30s`, `1m`)

### 용어 설명
- `LOAD_VUS`는 로그인 완료 상태를 의미하지 않고, k6가 요청을 실행할 수 있도록 미리 확보한 가상 사용자 풀 크기다.
- `LOAD_RATE`는 전체 합산 RPS다. 예를 들어 `LOAD_RATE=100`, `LOAD_VUS=10`이면 평균적으로 각 VU가 초당 10건을 수행하게 된다.
- 위 설정은 “사용자 수가 적고 클릭 빈도가 높은” 패턴을 만들 수 있으므로, 실제 사용자 행태를 반영하려면 VU 수를 늘리고 RPS를 낮추는 방식이 더 자연스럽다.

### 10명 테스트 실행 예시
```bash
docker run --rm -i grafana/k6 run - \
  -e BASE_URL=http://10.0.2.6:8080 \
  -e EMAIL_PREFIX=loadtest.user \
  -e EMAIL_DOMAIN=example.com \
  -e PASSWORD='Passw0rd!123' \
  -e RESTAURANT_ID=4 \
  -e SLOT_DATE=2026-01-16 \
  -e SLOT_TIME=12:00 \
  -e PARTY_SIZE=4 \
  -e RESERVATION_TYPE=RESERVATION_DEPOSIT \
  -e LOAD_RATE=10 \
  -e LOAD_VUS=10 \
  -e LOAD_MAX_VUS=20 \
  -e LOAD_DURATION=30s \
  < scripts/k6_reservation_loadtest.js
```

## 사용자당 1회 예약 테스트 (per-vu-iterations)
- 경로: `scripts/k6_reservation_loadtest_once.js`
- 설명: 각 VU가 로그인 후 예약 생성 1회만 수행

```bash
docker run --rm -i grafana/k6 run - \
  -e BASE_URL=http://10.0.2.6:8080 \
  -e EMAIL_PREFIX=loadtest.user \
  -e EMAIL_DOMAIN=example.com \
  -e PASSWORD='Passw0rd!123' \
  -e RESTAURANT_ID=6 \
  -e SLOT_DATE=2026-01-16 \
  -e SLOT_TIME=12:00 \
  -e PARTY_SIZE=4 \
  -e RESERVATION_TYPE=RESERVATION_DEPOSIT \
  -e LOAD_VUS=10 \
  -e LOAD_DURATION=30s \
  < scripts/k6_reservation_loadtest_once.js
```

## 모니터링 가이드
- Scouter XLog: 예약 API 응답 시간, SQL/Redis 오류 스택 확인
- Scouter Host: WAS/DB/Redis 컨테이너 CPU/메모리/IO 확인
- MySQL: slow query log 또는 performance_schema(필요 시)
- Redis: slowlog/latency monitor(필요 시)

## 1차 부하 테스트 결과 (100 RPS / 1분)
- 실행 환경: bastion에서 Docker k6 실행
- 대상: `/api/login`, `/api/reservations`
- 목표: 100 RPS, 100 VU (max 200)
- 결과 요약:
  - Insufficient VUs 경고 발생: 목표 RPS 유지 실패
  - dropped_iterations 3719: 목표 요청 대량 유실
  - login 성공률 약 9% (100/1036)
  - reservation 2xx/4xx 성공률 거의 0 (5/1346)
  - http_req_failed 95.59%
  - 응답시간 p95 약 18s 이상
  - 서버 로그에 잔여석 부족 예외 다수 발생

## 2차 부하 테스트 결과 (10 VU / 30초)
- 실행 환경: bastion에서 Docker k6 실행
- 시나리오: 10 VU, 30초 (looping VU)
- 결과 요약:
  - checks_succeeded 0.58% (15/2554)
  - reservation 2xx/4xx 성공률 0% (5/2539)
  - http_req_failed 99.41%
  - 응답시간 p95 약 23ms
  - 실패 대부분이 5xx로 추정(정상 실패 응답 미정)

## Scouter 분석 (k6-load-test-10_260111.csv)
- 소스: `scouter/k6-load-test-10_260111.csv`
- 총 트랜잭션: 200건
- 응답시간(ms): 평균 98.7 / p95 1623.35 / 최대 1761
- 서비스 분포: `/api/reservations` 98건, `/error` 92건, `/api/login<POST>` 10건
- SQL: 평균 1.155건/요청, SQL 시간 평균 4.41ms
- 오류: 93건 모두 `잔여석이 부족합니다. (남은 좌석: 1)` 예외

## 정원 확인 테스트 결과 (5 RPS / 5초)
- 실행 환경: bastion에서 Docker k6 실행
- 시나리오: 5 RPS, 5 VU, 5초
- 결과 요약:
  - checks_succeeded 100% (31/31)
  - http_req_failed 67.74% (4xx 포함)
  - 응답시간 p95 약 75.94ms
  - 실제 VU 사용량이 1까지 떨어짐(요청 처리 속도가 빨라 VU가 많이 필요하지 않은 상태)

### 예약 건수 확인 결과
- 슬롯 상태별 집계:
  - TEMPORARY: 5건 / 20명
  - EXPIRED: 12건 / 48명
- 활성 예약 합계: 5건 / 20명

## 사용자당 1회 예약 테스트 결과 (k6-load-test-once_260111.csv)
- 소스: `scouter/k6-load-test-once_260111.csv`
- 총 트랜잭션: 20건 (로그인 10, 예약 10)
- 응답시간(ms): 평균 199.6 / 최대 418 (p95는 샘플 수 부족으로 계산 생략)
- SQL: 평균 2.9건/요청, SQL 시간 평균 7.45ms
- 오류: 0건

### 로그인/예약 응답시간 분리
- 로그인: 평균 375.5ms / 최대 418ms (10건)
- 예약: 평균 23.7ms / 최대 50ms (10건)

### 로그인 지연 원인 가설 및 대응
- 가설:
  - BCrypt 해시 검증 비용이 커서 로그인 지연이 발생
  - 사용자 조회 쿼리/인덱스 부재로 DB 지연 발생
  - 동시에 여러 로그인 요청이 들어오면서 CPU 경합 증가
- 대응:
  - BCrypt cost factor 확인(운영에서 과도하게 높지 않은지)
  - `users.email` 인덱스 확인(UNIQUE로 존재해야 함)
  - 로그인 부하 테스트는 사전 토큰 발급 후 제외하는 시나리오 병행
  - 로그인/예약을 분리 테스트하여 병목 구간 명확화

## 정상 동작 베이스라인 (1회 예약 테스트 기준)
- 기준 시나리오: 10 VU, 사용자당 1회 로그인 + 1회 예약
- 기대 결과:
  - 로그인/예약 모두 2xx 또는 정상 4xx
  - Scouter 오류 0건
  - 평균 응답시간 200ms 내외(현재 199.6ms)
  - SQL 평균 3건 내외, SQL 시간 평균 10ms 이하

### 부하 단계별 기준 (초안)
- **Baseline A (1회 예약)**: 10 VU, per-vu-iterations 1회
  - 응답시간: avg ~200ms, max < 500ms
  - 오류: 0건
- **Baseline B (정원 확인)**: 5 RPS, 5초, constant-arrival-rate
  - 기대: 2xx + 정상 4xx 혼합, 5xx는 0에 근접
  - 응답시간: p95 < 100ms
- **Baseline C (한계 탐색)**: 100 RPS, 30초, constant-arrival-rate
  - 기대: dropped_iterations 증가, 5xx 증가 가능
  - 목적: 처리 한계/병목 파악

## 테스트 매트릭스 (템플릿)
| 테스트 ID | 목적 | 시나리오 | VU/Rate/Duration | 슬롯 | 파티사이즈 | 결과 요약 | p95(ms) | 5xx 비율 | dropped_iterations | 비고 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| T01 | Baseline A | per-vu-iterations | VU=10 / 1회 | R=6, 2026-01-16 12:00 | 4 | 로그인 10, 예약 10, 오류 0 | 426.8 | 0% (추정) | 0 | scouter 평균 199.6ms |
| T02 | Baseline B | constant-arrival-rate | 5 RPS / 5s | R=6, 2026-01-16 12:00 | 4 | checks 100%, 예약 5건/20명 | 75.94 | 0% (추정) | 0 | 4xx 포함 |
| T03 | Baseline C | constant-arrival-rate | 100 RPS / 30s | R=6, 2026-01-16 12:00 | 4 | 과부하, 실패 다수 | 4690 | 미집계 (http_req_failed 95.73%) | 613 | VU 200까지 증가 |
## 관찰 포인트
- 성공/실패 비율
- 평균/최대 응답 시간
- DB/Redis 오류 발생 여부
- Scouter XLog의 SQL/Redis 오류 스택 유무

---

## 기능 요건 (대기열/예상시간)
### 1) 실패 시 대기 등록
- 제한 인원 초과 시, 즉시 실패가 아니라 대기 등록 옵션 제공
- 대기 등록 응답에는 아래 정보를 포함
  - 대기 순번
  - 예상 대기 시간(ETA)
  - 현재 대기 인원

### 2) 예상 시간 산정
- 기준: 평균 예약 처리 시간 또는 슬롯 회전률
- 초기에는 단순 추정(예: 고정 배수)으로 시작 가능
- 향후 실제 처리 데이터 기반으로 조정 가능

### 3) UI 요구사항
- 예약 실패 모달에서 다음을 표시
  - 대기 가능 여부
  - 예상 대기 시간
  - 현재 대기 인원
  - 실패 가능성 경고 문구

### 4) API 응답 형태(초안)
- 예약 성공: `{ reservationId, ... }`
- 예약 실패/대기 가능: `{ code, message, waitAvailable, waitCount, etaSeconds }`
- 대기 등록 완료: `{ waitId, waitPosition, etaSeconds }`

### 5) 서버 측 처리(초안)
- 대기열은 Redis 기반 큐로 관리
- 예약 슬롯별 대기열 키 분리
- 예약 취소/만료 시 대기열에서 자동 승격 처리 필요

---

## 결정 필요 사항
- 인증 방식(토큰/세션) 및 부하 테스트에 사용할 계정 수
- 실패 응답 코드(409 vs 429 등) 정책
- 대기열 저장소(Redis) 및 만료 정책
- ETA 산정 로직(고정/이동 평균/슬롯 기반)

---

## 구현 단계 (추천 순서)
1) **부하 테스트 준비**
   - 테스트 계정/토큰 발급 및 목록 정리
   - 테스트 대상 슬롯/레스토랑 고정
   - k6/Locust 스크립트 초안 작성
2) **예약 실패 응답 표준화**
   - 좌석 초과/중복 예약 시 응답 코드와 메시지 정의
   - 프론트에서 처리할 수 있도록 응답 스키마 통일
3) **대기열 API 설계**
   - 대기 등록/조회/해제 API 설계
   - 응답에 `waitPosition`, `waitCount`, `etaSeconds` 포함
4) **대기열 저장소 구현**
   - Redis 키 설계(슬롯별 대기열)
   - 대기열 등록/팝/만료 처리
5) **승격 로직**
   - 예약 취소/만료 시 다음 대기자를 예약 가능 상태로 승격
6) **UI/UX 적용**
   - 실패 모달에 대기 안내/예상시간/대기 인원 표시
7) **부하 테스트 실행 및 튜닝**
   - 100 동시/100 RPS 시나리오 실행
   - 성공/실패 비율, 응답 시간, DB/Redis 부하 확인
   - 임계값 조정(ETA/대기열 크기/타임아웃)

## 테스트 계정 생성 SQL (직접 실행)
MySQL 5.7+ 기준으로 숫자 파생 테이블로 100명을 생성한다.

```sql
USE lunchgo;

SET @pwd_hash = '$2y$10$t4UwtOLvLipklqEUg6SnjePE62yzPrt3z6I.bGy9l3ONYcG/CQCyi';

INSERT INTO users (
  email, password, name, nickname, phone, birth, gender, image,
  company_name, company_address, status, marketing_agree, email_authentication, role
)
SELECT
  CONCAT('loadtest.user', LPAD(seq, 4, '0'), '@example.com'),
  @pwd_hash,
  CONCAT('LoadTest', LPAD(seq, 4, '0')),
  NULL,
  CONCAT('010-9000-', LPAD(seq, 4, '0')),
  NULL,
  'NONE',
  NULL,
  'LoadTest Co',
  'Seoul',
  'ACTIVE',
  0,
  0,
  'USER'
FROM (
  SELECT ones.n + tens.n * 10 + 1 AS seq
  FROM (
    SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
    UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
  ) ones
  CROSS JOIN (
    SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
    UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
  ) tens
  WHERE ones.n + tens.n * 10 < 100
) seqs
ON DUPLICATE KEY UPDATE email = email;
```
