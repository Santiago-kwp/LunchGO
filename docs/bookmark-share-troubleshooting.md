# 즐겨찾기 공유 트러블슈팅 기록

## 문제 1: 즐겨찾기 목록 조회 500 에러 (Boolean/Byte 변환 오류)

### 증상
- `GET /api/bookmark/list?userId=...` 호출 시 500 에러
- 서버 로그에 `Cannot project java.lang.Boolean/Byte to java.lang.*` 발생

### 원인
- `native query` 결과에서 `TINYINT(0/1)` 컬럼이 JDBC에서 `Byte` 또는 `Boolean`으로 반환됨
- Projection 인터페이스(`BookmarkListRow`) 타입과 실제 반환 타입이 불일치하여 변환 실패

### 해결
- SQL에서 `promotionAgree`, `isPublic`을 `CASE WHEN ... THEN 1 ELSE 0` 형태로 고정
- Projection 타입을 `Integer`로 고정하고, 서비스에서 `== 1`로 boolean 변환

### 해결 과정
- 동일 API가 조회 데이터에 따라 `Byte` 또는 `Boolean`으로 반환되는 것을 로그로 확인
- Projection 타입을 `Boolean` → `Byte` → `Integer`로 변경하며 재현
- SQL에서 숫자로 강제 반환하도록 변경 후 안정화 확인

### 적용 파일
- `src/main/java/com/example/LunchGo/bookmark/repository/BookmarkRepository.java`
- `src/main/java/com/example/LunchGo/bookmark/repository/BookmarkListRow.java`
- `src/main/java/com/example/LunchGo/bookmark/service/BaseBookmarkService.java`
