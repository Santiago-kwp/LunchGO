# 트렌딩 식당 추천 구현 정리 (Ver 1.1)

작성일: 2025-12-27
담당: 추천 시스템

## 목표
- 최근 7일 기준 트렌드(인기순) 식당 추천
- 신규 입점 식당의 Cold Start 완화(Newbie Boost)
- 대표 이미지/태그 포함 응답

## 정책 결정 사항
- 점수 공식
  - Score = (Confirm_recent * W_confirm) + (View_recent * W_view) + W_new
- Confirm_recent: 최근 7일 예약 확정 수
- View_recent: 최근 7일 조회 수
- W_new (신규성 가중치): 오픈 후 30일은 고정, 이후 30일간 선형 감쇠
  - Day_diff = 오늘 날짜 - created_at
  - 0~30일: W_new = C
  - 31~60일: W_new = C * (60 - Day_diff) / 30
  - 61일 이후: W_new = 0
  - C 기본값: 50
- W_confirm 기본값: 0.8
- W_view 기본값: 0.2

## 사용 데이터
- daily_restaurant_stats
  - stat_date, restaurant_id, view_count, confirm_count
- restaurants
  - created_at, status, name, address
- restaurant_images
  - 대표 이미지 1장 사용(가장 낮은 PK)
- restaurant_tag_maps + search_tags
  - tag_id, content

## API
- GET /api/restaurants/trending?days={n}&limit={m}
  - days 기본값 7, 최대 30
  - limit 기본값 10, 최대 50

## 응답 필드
- restaurantId, name, roadAddress, detailAddress
- viewCount, confirmCount, reviewCount, rating, score
- imageUrl (없으면 기본 이미지 반환)
- tags: [{ tagId, content }]
- reviewTags: [{ tagId, content, count }]

## 환경 변수
- TREND_NEWBIE_WEIGHT (default: 50)
- TREND_CONFIRM_WEIGHT (default: 0.8)
- TREND_VIEW_WEIGHT (default: 0.2)
- TREND_VISIT_WEIGHT (legacy, default: 0.8)
- TREND_DEFAULT_IMAGE_URL (default: /placeholder.svg)

## 구현 위치
- 쿼리: src/main/java/com/example/LunchGo/restaurant/repository/RestaurantRepository.java
- 서비스: src/main/java/com/example/LunchGo/restaurant/service/RestaurantTrendService.java
- 컨트롤러: src/main/java/com/example/LunchGo/restaurant/controller/RestaurantTrendController.java
- DTO: src/main/java/com/example/LunchGo/restaurant/dto/TrendingRestaurantItem.java

## 구현 코드 요약
### 1) 트렌딩 쿼리 (최근 n일 집계 + 신규성 가중치 + 이미지/태그)
```sql
SELECT
  r.restaurant_id AS restaurantId,
  r.name AS name,
  r.road_address AS roadAddress,
  r.detail_address AS detailAddress,
  COALESCE(s.view_recent, 0) AS viewCount,
  COALESCE(s.confirm_recent, 0) AS confirmCount,
  COALESCE((
    SELECT COUNT(*)
    FROM reviews rv
    WHERE rv.restaurant_id = r.restaurant_id
      AND rv.status = 'PUBLIC'
  ), 0) AS reviewCount,
  COALESCE((
    SELECT ROUND(AVG(rv.rating), 1)
    FROM reviews rv
    WHERE rv.restaurant_id = r.restaurant_id
      AND rv.status = 'PUBLIC'
  ), 0) AS rating,
  (
    (COALESCE(s.confirm_recent, 0) * :confirmWeight) +
    (COALESCE(s.view_recent, 0) * :viewWeight) +
    CASE
      WHEN r.created_at >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
      THEN :newbieWeight
      WHEN r.created_at >= DATE_SUB(CURDATE(), INTERVAL 60 DAY)
      THEN :newbieWeight * (60 - DATEDIFF(CURDATE(), r.created_at)) / 30
      ELSE 0
    END
  ) AS score,
  (
    SELECT ri.image_url
    FROM restaurant_images ri
    WHERE ri.restaurant_id = r.restaurant_id
    ORDER BY ri.restaurant_image_id
    LIMIT 1
  ) AS imageUrl,
  (
    SELECT GROUP_CONCAT(st.tag_id ORDER BY st.tag_id SEPARATOR ',')
    FROM restaurant_tag_maps rtm
    JOIN search_tags st ON st.tag_id = rtm.tag_id
    WHERE rtm.restaurant_id = r.restaurant_id
  ) AS tagIds,
  (
    SELECT GROUP_CONCAT(st.content ORDER BY st.tag_id SEPARATOR ',')
    FROM restaurant_tag_maps rtm
    JOIN search_tags st ON st.tag_id = rtm.tag_id
    WHERE rtm.restaurant_id = r.restaurant_id
  ) AS tagContents,
  (
    SELECT GROUP_CONCAT(t.tag_id ORDER BY t.tag_count DESC, t.tag_id SEPARATOR ',')
    FROM (
      SELECT rt.tag_id, COUNT(*) AS tag_count
      FROM reviews rv
      JOIN review_tag_maps rtm ON rv.review_id = rtm.review_id
      JOIN review_tags rt ON rt.tag_id = rtm.tag_id
      WHERE rv.restaurant_id = r.restaurant_id
        AND rv.status = 'PUBLIC'
        AND rt.tag_type = 'USER'
      GROUP BY rt.tag_id
      ORDER BY tag_count DESC, rt.tag_id
      LIMIT 3
    ) t
  ) AS reviewTagIds,
  (
    SELECT GROUP_CONCAT(t.name ORDER BY t.tag_count DESC, t.tag_id SEPARATOR ',')
    FROM (
      SELECT rt.tag_id, rt.name, COUNT(*) AS tag_count
      FROM reviews rv
      JOIN review_tag_maps rtm ON rv.review_id = rtm.review_id
      JOIN review_tags rt ON rt.tag_id = rtm.tag_id
      WHERE rv.restaurant_id = r.restaurant_id
        AND rv.status = 'PUBLIC'
        AND rt.tag_type = 'USER'
      GROUP BY rt.tag_id, rt.name
      ORDER BY tag_count DESC, rt.tag_id
      LIMIT 3
    ) t
  ) AS reviewTagContents,
  (
    SELECT GROUP_CONCAT(t.tag_count ORDER BY t.tag_count DESC, t.tag_id SEPARATOR ',')
    FROM (
      SELECT rt.tag_id, COUNT(*) AS tag_count
      FROM reviews rv
      JOIN review_tag_maps rtm ON rv.review_id = rtm.review_id
      JOIN review_tags rt ON rt.tag_id = rtm.tag_id
      WHERE rv.restaurant_id = r.restaurant_id
        AND rv.status = 'PUBLIC'
        AND rt.tag_type = 'USER'
      GROUP BY rt.tag_id
      ORDER BY tag_count DESC, rt.tag_id
      LIMIT 3
    ) t
  ) AS reviewTagCounts
FROM restaurants r
LEFT JOIN (
  SELECT restaurant_id,
         CAST(SUM(IFNULL(view_count, 0)) AS SIGNED) AS view_recent,
         CAST(SUM(IFNULL(confirm_count, 0)) AS SIGNED) AS confirm_recent
  FROM daily_restaurant_stats
  WHERE stat_date >= DATE_SUB(CURDATE(), INTERVAL :days DAY)
  GROUP BY restaurant_id
) s ON r.restaurant_id = s.restaurant_id
WHERE r.status = 'OPEN'
ORDER BY score DESC
LIMIT :limit
```
설명
- 기본 집계: `daily_restaurant_stats`를 최근 `:days` 기준으로 합산해 `viewCount/confirmCount` 생성.
- 리뷰 지표: `reviews`에서 `PUBLIC`만 대상으로 `reviewCount`, `rating`(소수 1자리) 계산.
- 신규성 가중치: `created_at` 기준 30일은 고정 보너스, 31~60일 선형 감쇠.
- 대표 이미지: `restaurant_images`에서 가장 낮은 PK 1장을 대표 이미지로 선택.
- 식당 태그: `restaurant_tag_maps + search_tags`를 통해 식당 자체 태그를 문자열로 집계.
- 리뷰 태그 상위 3개: `review_tag_maps + review_tags` 집계 후 `COUNT` 기준 상위 3개만 추출.
- GROUP_CONCAT 동작: 집계된 행을 하나의 문자열로 합치며, `SEPARATOR ','` 기준으로 콤마 구분 문자열 생성.
  - 예: tag_id 10,11,5 → `"10,11,5"` / tag_content `"재료가 신선해요,가격 대비 만족스러워요,인테리어가 세련돼요"`

실행 예시
```sql
GET /api/restaurants/trending?days=7&limit=5

[
  {
    "restaurantId": 1,
    "name": "숙성도 강남점",
    "viewCount": 220,
    "confirmCount": 16,
    "reviewCount": 8,
    "rating": 4.4,
    "score": 123.5,
    "imageUrl": "https://.../restaurant-1.jpg",
    "tags": [
      { "tagId": 3, "content": "룸" },
      { "tagId": 9, "content": "단체" }
    ],
    "reviewTags": [
      { "tagId": 10, "content": "재료가 신선해요", "count": 4 },
      { "tagId": 11, "content": "가격 대비 만족스러워요", "count": 3 },
      { "tagId": 5, "content": "인테리어가 세련돼요", "count": 2 }
    ]
  }
]
```

### 2) 서비스 매핑 (기본 이미지 + 태그 구조화)
```java
return rows.stream()
    .map(row -> TrendingRestaurantItem.builder()
        .restaurantId(row.getRestaurantId())
        .name(row.getName())
        .roadAddress(row.getRoadAddress())
        .detailAddress(row.getDetailAddress())
        .viewCount(valueOrZero(row.getViewCount()))
        .confirmCount(valueOrZero(row.getConfirmCount()))
        .reviewCount(valueOrZero(row.getReviewCount()))
        .rating(valueOrZero(row.getRating()))
        .score(valueOrZero(row.getScore()))
        .imageUrl(resolveImageUrl(row.getImageUrl()))
        .tags(buildTags(row.getTagIds(), row.getTagContents(), null))
        .reviewTags(buildTags(
            row.getReviewTagIds(),
            row.getReviewTagContents(),
            row.getReviewTagCounts()
        ))
        .build())
    .toList();
```
설명
- `reviewCount/rating`을 DTO에 포함해 트렌딩 카드에서 즉시 표시 가능.
- `tags`는 식당 태그, `reviewTags`는 리뷰 기반 상위 태그(카운트 포함).
- `resolveImageUrl()`로 대표 이미지가 없을 때 기본 이미지 URL을 보장.

### 3) 태그 파싱 로직 (식당 태그/리뷰 태그 공용)
```java
private List<TrendingTagItem> buildTags(String tagIds, String tagContents, String tagCounts) {
    if (tagIds == null || tagIds.isBlank() || tagContents == null || tagContents.isBlank()) {
        return Collections.emptyList();
    }
    String[] idParts = tagIds.split(",");
    String[] contentParts = tagContents.split(",");
    String[] countParts = tagCounts == null ? new String[0] : tagCounts.split(",");
    int size = Math.min(idParts.length, contentParts.length);
    return IntStream.range(0, size)
        .mapToObj(index -> {
            String idPart = idParts[index].trim();
            String contentPart = contentParts[index].trim();
            if (idPart.isEmpty() || contentPart.isEmpty()) {
                return null;
            }
            try {
                Integer countValue = null;
                if (countParts.length > index) {
                    String countPart = countParts[index].trim();
                    if (!countPart.isEmpty()) {
                        countValue = Integer.parseInt(countPart);
                    }
                }
                return TrendingTagItem.builder()
                    .tagId(Long.parseLong(idPart))
                    .content(contentPart)
                    .count(countValue)
                    .build();
            } catch (NumberFormatException ex) {
                return null;
            }
        })
        .filter(Objects::nonNull)
        .toList();
}
```
설명
- `GROUP_CONCAT`으로 받은 콤마 구분 문자열을 각각 분리.
  - tagIds: `"10,11,5"` → `["10","11","5"]`
  - tagContents: `"재료가 신선해요,가격 대비 만족스러워요,인테리어가 세련돼요"`
  - tagCounts: `"4,3,2"`
- 인덱스 기준으로 같은 위치의 값들을 매칭해 하나의 태그로 묶는다.
  - index 0 → tagId=10, content=재료가 신선해요, count=4
  - index 1 → tagId=11, content=가격 대비 만족스러워요, count=3
  - index 2 → tagId=5, content=인테리어가 세련돼요, count=2
- 파싱 실패(빈 문자열/숫자 변환 오류)는 `null` 처리 후 제거.
- 식당 태그는 count가 없으므로 `tagCounts`가 `null`일 때도 동작하도록 처리.

### 4) 환경 변수 적용 (신규성 가중치/기본 이미지)
```yaml
trend:
  newbie-weight: ${TREND_NEWBIE_WEIGHT:50}
  confirm-weight: ${TREND_CONFIRM_WEIGHT:0.8}
  view-weight: ${TREND_VIEW_WEIGHT:0.2}
  default-image-url: ${TREND_DEFAULT_IMAGE_URL:/placeholder.svg}
```

## 스코어 산출 로직(요약)
- 최근 n일의 view_count, confirm_count 집계
- 신규성 보너스: created_at 기준 30일 유지 + 이후 30일 선형 감쇠
- score 내림차순 정렬 후 상위 limit 반환

## 대표 이미지 처리
- restaurant_images가 비어있으면 기본 이미지 URL 반환

## 프론트 표시 규칙
- 트렌딩 응답의 reviewCount, rating, reviewTags를 우선 사용
- 리뷰 태그가 3개 이상(카운트 >= 1)일 때 리뷰 태그 표시
- 그렇지 않으면 식당 태그 표시 (신규 식당 대응)
- 트렌딩 리스트 상단 + 기존 리스트(중복 제거) 하단 구성

## 테스트 데이터 보강
- 리뷰/태그/이미지 샘플 데이터 생성 쿼리 추가
  - 파일: src/main/resources/sql/insert_mock_data_review.sql
- review_images_seed.sql은 기존 URL을 유지하고 현재 리뷰 목록에 순서 매핑
  - 파일: src/main/resources/sql/review_images_seed.sql
