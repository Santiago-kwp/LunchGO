# 📄점주용 주간 통계 AI 요약 PDF 구현 정리 

이 문서는 점주(Owner)가 주간 예약/매출 통계 데이터를 AI로 요약해 PDF로 내려받는 기능을 구현한 내용을 상세하게 정리한 문서입니다. 현재 프로젝트 기준으로 `BusinessRestaurantController`, `RestaurantStatsService`, `AiChatService`, `OpenAiConfig` 흐름을 중심으로 설명합니다.

대상 클래스

- `BusinessRestaurantController`
- `RestaurantStatsService`
- `AiChatService`
- `OpenAiConfig` (Gemini용 `ChatLanguageModel` 구성)

---

## 🔁전체 처리 흐름 

1) 사업자가 식당 ID 기준으로 요청
2) 최근 7일 예약/통계 데이터 수집
3) AI 요약 요청 (Gemini)
4) AI 결과를 PDF 본문으로 변환
5) PDF 파일 다운로드 응답 반환

---

## 📌API 엔드포인트 

`GET /api/business/restaurants/{id}/stats/weekly.pdf`

권한/검증

- `ROLE_OWNER` 권한 필요
- 로그인 사용자와 식당 소유자 일치 확인

응답 헤더

- `Content-Type: application/pdf`
- `Content-Disposition: attachment; filename=weekly-stats-{id}.pdf`
- `Content-Length: {pdfBytes}`

---

## ✅컨트롤러: `BusinessRestaurantController` 

경로: `src/main/java/com/example/LunchGo/restaurant/controller/BusinessRestaurantController.java`

역할

- 요청 인증 및 권한 검증
- 식당 소유자 확인
- 서비스 호출 후 PDF 다운로드 응답 반환

주요 처리

- 로그인 정보 없음 → `401`
- 식당 없음 → `404`
- 본인 소유 식당이 아님 → `403`
- 성공 시 PDF 바이트 + 헤더 설정하여 응답

---

## 🧠서비스: `RestaurantStatsService` 

경로: `src/main/java/com/example/LunchGo/restaurant/service/RestaurantStatsService.java`

역할

- 예약/통계 데이터 조회 및 정제
- AI 프롬프트 생성
- AI 요약 결과 수신
- PDF 문서 생성

### 데이터 수집

- `BusinessReservationQueryService.getList(restaurantId)`로 예약 리스트 조회
- `DailyRestaurantStatsRepository.findLast7DaysByRestaurantId`로 최근 7일 통계 조회

### AI 프롬프트 구성 

AI에게 아래 섹션을 포함해서 한국어 요약을 생성하도록 요청

- `## 요약`
- `## 인사이트`
- `## 개선점 및 추천`

포함 데이터 예시

- 예약/방문/노쇼/벌금/매출 지표
- 일자별 증감 패턴
- 평균 대비 유의미한 변화

### PDF 출력 구성 📄

- PDFBox 사용
- 한글 폰트 로딩 필수
- 페이지 여백 적용 (상/하/좌/우)
- AI 요약 결과의 Markdown 스타일을 일부 렌더링
- `PdfCursor`로 자동 페이지 분할 처리

### 예외 처리 (HTTP 상태 매핑) 🚨

`ResponseStatusException`으로 매핑

- 쿼터 초과 → `429`
- 모델 없음 → `502`
- 타임아웃 → `504`
- 기타 AI 오류 → `502`
- PDF 생성 오류 → `500`

---

## 🤖AI 서비스 - `AiChatService` 

경로: `src/main/java/com/example/LunchGo/ai/service/AiChatService.java`

역할

- `ChatLanguageModel.generate(prompt)`로 AI 요약 호출
- 결과 문자열을 그대로 반환

특징

- 비즈니스 로직은 서비스에 집중시키고, AI 호출 책임만 분리
- 향후 모델 교체나 프롬프트 개선 시 영향 범위를 최소화

---

## 🔧설정 -  `OpenAiConfig` 

경로: `src/main/java/com/example/LunchGo/common/config/OpenAiConfig.java`

역할

- Gemini용 `ChatLanguageModel` Bean 구성

설정 키

- `gemini.api-key`
- `gemini.model`

참고

- 클래스명은 `OpenAiConfig`지만 실제 구현은 Gemini 기반
- 추후 OpenAI/Anthropic 등으로 전환 시 이 파일에서 교체 가능

---

## 설정 예시 ⚙️

```
gemini.api-key=YOUR_KEY
gemini.model=gemini-2.0-flash
pdf.korean-font-path=C:/Windows/Fonts/malgun.ttf
```

---

## 프론트엔드 호출 🖱️

`BusinessReservationsPage.vue`에서:

- `GET /api/business/restaurants/{id}/stats/weekly.pdf`
- `responseType: 'blob'`
- 파일명: `weekly-stats-{id}.pdf`

다운로드 흐름

- Blob 생성 → `URL.createObjectURL` → `<a>` 클릭 트리거
- 응답 헤더와 무관하게 프론트에서 파일명 지정 가능

---

## 운영/주의 사항 ✅

- AI 응답이 길 경우 자동 줄바꿈 + 페이지 분할 필요
- 한글 폰트 미설정 시 글자 깨짐 발생
- 쿼터 초과/타임아웃 등 오류는 사용자에게 명확한 메시지 필요



