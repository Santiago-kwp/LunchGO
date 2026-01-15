# 사업자 AI 인사이트 개선 설계

## 목표
기존 주간 PDF 인사이트에 "금주 방문 추이 예측" 섹션을 추가한다. 예측은 금주 구내식당 메뉴, 사용자 취향, 즐겨찾기 공유, 회사 정보 데이터를 활용하며 AI 응답 실패 시 규칙 기반으로 대체한다.

## 현재 엔드포인트 파악
`GET /api/business/restaurants/{id}/stats/weekly.pdf`

### 처리 흐름
1. `BusinessRestaurantController`에서 소유자 검증 후 서비스 호출.
2. `RestaurantStatsService.generateWeeklyStatsPdf`에서 최근 7일 예약 + 일별 통계 조회.
3. `AiChatService`로 주간 요약 프롬프트 생성 후 AI 응답 수신.
4. AI 요약을 PDF로 렌더링하여 다운로드 응답 반환.

### 현재 입력 데이터
- 예약 리스트: `BusinessReservationQueryService.getList(restaurantId)`
- 일별 통계: `DailyRestaurantStatsRepository.findLast7DaysByRestaurantId`

### 현재 출력 구성
AI 요약 섹션: `## 핵심 요약`, `## 상세 분석`, `## 통합 분석 및 추천`

## 개선 목표 범위
- 기존 PDF 요약에 "금주 방문 추이 예측" 섹션을 추가.
- 예측 데이터 근거를 PDF 내 요약으로 설명.
- AI 장애 시 규칙 기반 예측 결과 제공.

## 데이터 소스 매핑
| 데이터 | 테이블/스키마 |
| --- | --- |
| 금주 구내식당 메뉴 | `cafeteria_menus` |
| 사용자 취향 | `specialities`, `speciality_mappings` |
| 즐겨찾기 공유 | `bookmarks`, `bookmark_links` |
| 회사 정보 | `users.company_name`, `users.company_address` |
| 방문/예약 히스토리 | `reservations`, `restaurant_reservation_slots`, `reservation_visit_stats` |

## 단계별 설계

### 1) 요구 정의 및 출력 포맷 확정
- PDF에 `## 금주 방문 예측` 섹션 추가.
- 예측 결과 형식: 요일별 예상 방문 범위 + 핵심 근거 2~3줄.
- 프론트 다운로드 플로우는 기존 엔드포인트 유지.

### 2) 데이터 수집 레이어 확장
- `RestaurantStatsService`에 금주 예측용 데이터 조회 추가.
- 신규 Repository/Mapper 추가:
  - `cafeteria_menus` (금주 범위, user_id 기준)
  - `speciality_mappings` + `specialities` (사용자 취향)
  - `bookmarks`, `bookmark_links` (공유 네트워크 및 공개 북마크)
  - `reservations` + `restaurant_reservation_slots` (과거 예약 추이)
  - `reservation_visit_stats` (재방문 패턴)

### 3) 피처(지표) 계산
- 기본 수요: 최근 8~12주 요일별 예약 평균 + 변동성.
- 구내식당 메뉴 시그널:
  - `cafeteria_menus.main_menu_names`에서 키워드 추출.
  - 사용자 취향 키워드와 매칭 비율 산출.
- 공유 네트워크 시그널:
  - `bookmark_links` 승인 관계 + 공개 북마크 확산 지수.
  - 네트워크 밀도에 따라 가중치 부여.
- 회사 그룹 시그널:
  - `users.company_name` 기준 동일 회사 방문 비율 추정.

### 4) AI 프롬프트 확장
- 기존 주간 통계 프롬프트에 예측 지표를 추가.
- 요구 섹션에 `## 금주 방문 예측`을 명시.
- 입력 데이터는 요약 지표만 전달 (원시 데이터 최소화).

### 5) 규칙 기반 fallback 설계
- AI 실패/타임아웃 시 아래 로직으로 예측치 생성:
  - 기본 수요(주간 평균) * (메뉴 매칭 지수) * (공유 네트워크 가중치).
  - 가중치는 0.9~1.1 범위로 제한하여 과대 예측 방지.
  - 결과에 "규칙 기반 추정" 문구 포함.

### 6) PDF 출력 구성 개선
- PDF에 예측 섹션 추가:
  - 요일별 예상 방문 범위 (예: 월 18~24명)
  - 주요 근거 (메뉴 매칭 상위 키워드, 공유 증가율)
  - 신뢰도(LOW/MEDIUM/HIGH)

### 7) 검증 및 운영 지표
- A/B 또는 기간 전후 비교로 예측 적중률 체크.
- 데이터 공백(메뉴 미업로드, 공유 없음) 시 fallback 동작 확인.
- PDF 생성 시간 및 AI 호출 실패율 모니터링.

## 개발 체크리스트
- [ ] 데이터 수집 쿼리/리포지토리 추가
- [ ] 예측 지표 계산 로직 구현
- [ ] 프롬프트에 예측 섹션 포함
- [ ] fallback 메시지 생성
- [ ] PDF 렌더링 섹션 추가
- [ ] 로그/모니터링 지표 추가

## 리스크 및 대응
- 메뉴 데이터 누락: 기본 수요만으로 예측 (가중치 1.0).
- 사용자 취향 적재 부족: 회사/예약 기반 가중치 중심으로 보정.
- 공유 네트워크 희소: 공유 가중치 무시.
- AI 오류: 즉시 규칙 기반으로 전환하고 문구 명시.

## 작업 계획 (쿼리/DTO/서비스)

### 1) 쿼리 설계
- 예약 기반 수요 베이스라인
  - 최근 8~12주 요일별 예약 수 집계 (restaurant_id 기준)
  - `reservations` + `restaurant_reservation_slots` 조인
  - 출력: `day_of_week`, `avg_count`, `stddev` (가능하면)
- 구내식당 메뉴 데이터
  - `cafeteria_menus`에서 금주(월~금) 데이터 조회
  - `user_id`, `served_date`, `main_menu_names` (json)
- 사용자 취향 데이터
  - `speciality_mappings` + `specialities`로 user_id별 취향 키워드
  - 출력: `user_id`, `keyword`, `is_liked`
- 즐겨찾기 공유 신호
  - `bookmark_links` (승인 상태)로 네트워크 연결 수
  - `bookmarks`에서 restaurant_id 기준 공개 북마크 수
- 회사 정보
  - `users`에서 `company_name`, `company_address` 추출
  - 같은 회사 사용자군 비중 계산용

### 2) DTO 설계
- `WeeklyDemandSignalResponse`
  - `baselineByWeekday` (요일별 평균/변동성)
  - `cafeteriaMenuSignals` (메뉴 키워드, 날짜)
  - `preferenceSignals` (키워드 매칭율)
  - `shareSignals` (공유 네트워크 지수)
  - `companySignals` (회사별 방문 비중)
- `WeeklyDemandPrediction`
  - `weekday`, `expectedMin`, `expectedMax`
  - `confidence` (LOW/MEDIUM/HIGH)
  - `evidence` (문장 리스트)

### 3) 서비스 분해
- `RestaurantStatsService`
  - 기존 주간 요약 + 예측 결과를 함께 생성
  - `buildPrompt`에 예측 지표 요약 추가
  - AI 실패 시 `RuleBasedForecastService` 호출
- `WeeklyDemandSignalService`
  - 데이터 수집/집계 전담
  - 쿼리 실행 후 `WeeklyDemandSignalResponse` 구성
- `RuleBasedForecastService`
  - 베이스라인 * (메뉴 매칭) * (공유 가중치) * (회사 가중치)
  - 가중치 범위 제한 및 증거 문구 생성

### 4) PDF 렌더링 변경
- `buildPdf`에 `## 금주 방문 예측` 섹션 추가
- 예측 테이블(요일/최소/최대) + 근거 bullet 렌더

### 5) 로깅/모니터링
- AI 요청 성공/실패 메트릭 추가
- fallback 사용 여부 로깅 (restaurant_id, week_range)
