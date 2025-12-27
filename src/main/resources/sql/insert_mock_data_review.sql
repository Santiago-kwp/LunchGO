/* ==================================================
   0. 초기화 (기존 데이터 삭제)
   ================================================== */
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE review_tag_maps;
TRUNCATE TABLE review_images;
TRUNCATE TABLE comments;
TRUNCATE TABLE reviews;
TRUNCATE TABLE review_tags;
TRUNCATE TABLE receipt_items;
TRUNCATE TABLE receipts;
SET FOREIGN_KEY_CHECKS = 1;


/* ==================================================
   1. 기초 데이터 (예약, 영수증)
   ================================================== */

-- 1-2. 영수증 (Receipts)
INSERT INTO receipts (receipt_id, reservation_id, confirmed_amount, image_url, created_at) VALUES
    (1, 1, 120000, 'https://s3-bucket.com/receipts/receipt_001.jpg', DATE_SUB(NOW(), INTERVAL 2 DAY));

INSERT INTO receipts (receipt_id, reservation_id, confirmed_amount, image_url, created_at) VALUES
    (2, 2, 60000, 'https://s3-bucket.com/receipts/receipt_002.jpg', DATE_SUB(NOW(), INTERVAL 2 DAY));


-- 1-3. 영수증 메뉴 상세 (Receipt Items)
INSERT INTO receipt_items (receipt_item_id, receipt_id, menu_name, qty, unit_price, total_price) VALUES
    (1, 1, '숙성 삼겹살', 4, 18000, 72000),
    (2, 1, '김치찌개', 1, 8000, 8000),
    (3, 1, '소주', 4, 5000, 20000),
    (4, 1, '맥주', 4, 5000, 20000);


/* ==================================================
   2. 리뷰 태그 (Review Tags) - ID 명시적 지정
   - 중복 제거 및 카테고리별 정렬
   ================================================== */
INSERT INTO review_tags (tag_id, name, category, tag_type)
VALUES
-- EFFICIENT
(1, '주문 즉시 조리 시작해요', 'EFFICIENT', 'USER'),
(2, '계산이 빨라요', 'EFFICIENT', 'USER'),
(3, '웨이팅 관리가 잘 돼요', 'EFFICIENT', 'USER'),
(4, '음식이 동시에 나와요', 'EFFICIENT', 'USER'),

-- ATMOSPHERE
(5, '인테리어가 세련돼요', 'ATMOSPHERE', 'USER'),
(6, '조명이 아늑해요', 'ATMOSPHERE', 'USER'),
(7, '아이 동반하기 좋아요', 'ATMOSPHERE', 'USER'),
(8, '야외 테라스가 있어요', 'ATMOSPHERE', 'USER'),
(9, '음악이 적당해요', 'ATMOSPHERE', 'USER'),

-- TASTE
(10, '재료가 신선해요', 'TASTE', 'USER'),
(11, '가격 대비 만족스러워요', 'TASTE', 'USER'),
(12, '시그니처 메뉴가 있어요', 'TASTE', 'USER'),
(13, '디저트가 맛있어요', 'TASTE', 'USER'),
(14, '술과 안주 궁합이 좋아요', 'TASTE', 'USER'),

-- SERVICE
(15, '직원들이 적극적으로 도와줘요', 'SERVICE', 'USER'),
(16, '메뉴 설명을 잘 해줘요', 'SERVICE', 'USER'),
(17, '결제 방식이 다양해요 (QR, 간편결제 등)', 'SERVICE', 'USER'),
(18, '반려동물 동반 가능해요', 'SERVICE', 'USER'),
(19, '청결 관리가 잘 돼요', 'SERVICE', 'USER'),
(20, '아기랑 같이 오기 편해요', 'SERVICE', 'USER'),

-- BLINDED (ADMIN 전용)
(21, '욕설/비속어 포함', 'BLINDED', 'ADMIN'),
(22, '인신공격/명예훼손', 'BLINDED', 'ADMIN'),
(23, '허위 사실 유포', 'BLINDED', 'ADMIN'),
(24, '도배/스팸/광고', 'BLINDED', 'ADMIN'),
(25, '경쟁 업체 비방', 'BLINDED', 'ADMIN');



/* ==================================================
   3. 리뷰 데이터 (Reviews)
   ================================================== */

-- Case 1: [Best] 영수증 인증 + 포토 + 칭찬 (이대리 -> 숙성도)
INSERT INTO reviews (review_id, restaurant_id, user_id, receipt_id, rating, content, created_at, updated_at, status) VALUES
    (1, 1, 2, 1, 5, '팀 회식으로 갔는데 고기 질이 정말 좋네요! 술이랑 궁합도 좋고, 직원분들이 고기를 다 구워주셔서 편했습니다. 재방문 의사 100%입니다.', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), 'PUBLIC');

-- Case 2: [Normal] 영수증 없음 + 텍스트만 (박과장 -> 숙성도)
INSERT INTO reviews (review_id, restaurant_id, user_id, receipt_id, rating, content, created_at, updated_at, status) VALUES
    (2, 1, 3, NULL, 4, '맛은 있는데 저녁 시간대라 웨이팅이 좀 있었어요. 그래도 기다린 보람이 있는 맛입니다.', NOW(), NOW(), 'PUBLIC');

-- Case 3: [Soso] 영수증 없음 + 포토 (최신입 -> 바다향기)
INSERT INTO reviews (review_id, restaurant_id, user_id, receipt_id, rating, content, created_at, updated_at, status) VALUES
    (3, 2, 4, NULL, 3, '회는 신선한데 가격대가 좀 있네요. 법카 쓸 때만 올 수 있을 듯...', NOW(), NOW(), 'PUBLIC');

-- Case 4: [Blinded] 블라인드 처리된 악성 리뷰 (익명/User2 -> 바다향기)
-- 관리자에 의해 숨김 처리된 상태 테스트용
INSERT INTO reviews (review_id, restaurant_id, user_id, receipt_id, rating, content, created_at, updated_at, status) VALUES
    (4, 2, 2, NULL, 1, '야이 $*!@ 주인장 나와라 장사를 이따위로 하냐?', DATE_SUB(NOW(), INTERVAL 5 DAY), NOW(), 'BLINDED');

INSERT INTO reviews (review_id, restaurant_id, user_id, receipt_id, rating, content, created_at, updated_at, status) VALUES
    (6, 1, 2, 2, 3, '영수증 이미지 테스트용 리뷰', DATE_SUB(NOW(), INTERVAL 5 DAY), NOW(), 'BLINDED');

INSERT INTO reviews (review_id, restaurant_id, user_id, receipt_id, rating, content, created_at, updated_at, status) VALUES
    (7, 1, 2, 2, 3, '테스트 리뷰', DATE_SUB(NOW(), INTERVAL 5 DAY), NOW(), 'PUBLIC');
INSERT INTO reviews (review_id, restaurant_id, user_id, receipt_id, rating, content, created_at, updated_at, status) VALUES
    (8, 1, 2, 2, 3, '테스트 리뷰', DATE_SUB(NOW(), INTERVAL 5 DAY), NOW(), 'PUBLIC');
INSERT INTO reviews (review_id, restaurant_id, user_id, receipt_id, rating, content, created_at, updated_at, status) VALUES
    (9, 1, 2, 2, 3, '테스트 리뷰', DATE_SUB(NOW(), INTERVAL 5 DAY), NOW(), 'PUBLIC');
INSERT INTO reviews (review_id, restaurant_id, user_id, receipt_id, rating, content, created_at, updated_at, status) VALUES
    (10, 1, 2, 2, 3, '테스트 리뷰', DATE_SUB(NOW(), INTERVAL 5 DAY), NOW(), 'PUBLIC');
INSERT INTO reviews (review_id, restaurant_id, user_id, receipt_id, rating, content, created_at, updated_at, status) VALUES
    (11, 1, 2, 2, 3, '테스트 신고 요청 리뷰', DATE_SUB(NOW(), INTERVAL 5 DAY), NOW(), 'BLIND_REQUEST');
INSERT INTO reviews (review_id, restaurant_id, user_id, receipt_id, rating, content, created_at, updated_at, status) VALUES
    (12, 1, 2, 2, 3, '테스트 신고 요청 거부된 리뷰', DATE_SUB(NOW(), INTERVAL 5 DAY), NOW(), 'BLIND_REJECTED');
INSERT INTO reviews (review_id, restaurant_id, user_id, receipt_id, rating, content, created_at, updated_at, status) VALUES
    (13, 1, 1, NULL, 5, '예약 시간에 맞춰 입장했고, 직원 응대가 친절했습니다.', DATE_SUB(NOW(), INTERVAL 3 DAY), NOW(), 'PUBLIC');
INSERT INTO reviews (review_id, restaurant_id, user_id, receipt_id, rating, content, created_at, updated_at, status) VALUES
    (14, 1, 2, NULL, 2, '리뷰 내용이 공격적이라 블라인드가 필요해 보입니다.', DATE_SUB(NOW(), INTERVAL 2 DAY), NOW(), 'BLINDED');
INSERT INTO reviews (review_id, restaurant_id, user_id, receipt_id, rating, content, created_at, updated_at, status) VALUES
    (15, 1, 3, NULL, 4, '음식이 빨리 나와서 회식 진행이 수월했습니다.', DATE_SUB(NOW(), INTERVAL 1 DAY), NOW(), 'PUBLIC');




/* ==================================================
   4. 리뷰 태그 매핑 (Review Tag Maps)
   ================================================== */

-- Review 1 (회식, 칭찬): 술궁합(14), 직원친절(15), 청결(19)
INSERT INTO review_tag_maps (review_id, tag_id) VALUES (1, 14), (1, 15), (1, 19);

-- Review 2 (웨이팅, 맛): 재료신선(10), 웨이팅관리(3)
INSERT INTO review_tag_maps (review_id, tag_id) VALUES (2, 10), (2, 3);

-- Review 3 (가격, 신선): 재료신선(10), 가격대비(11) - 리뷰 내용은 비싸다지만 태그는 보통 사용자가 선택하므로
INSERT INTO review_tag_maps (review_id, tag_id) VALUES (3, 10);

-- Review 4 (블라인드): 욕설(21), 인신공격(22) - 관리자가 태그를 달았다고 가정
INSERT INTO review_tag_maps (review_id, tag_id) VALUES (4, 21), (4, 22);

-- Review 6 (블라인드): 욕설(21)
INSERT INTO review_tag_maps (review_id, tag_id) VALUES (6, 21);

-- Review 7~10 (일반): 재료신선(10), 가격대비(11), 분위기(5)
INSERT INTO review_tag_maps (review_id, tag_id) VALUES
    (7, 10), (7, 11),
    (8, 5),
    (9, 10),
    (10, 11);

-- Review 11 (블라인드 요청): 도배/스팸(24)
INSERT INTO review_tag_maps (review_id, tag_id) VALUES (11, 24);

-- Review 12 (블라인드 거부): 인신공격(22)
INSERT INTO review_tag_maps (review_id, tag_id) VALUES (12, 22);

-- Review 13~15 (일반/블라인드): 직원친절(15), 인테리어(5), 욕설(21)
INSERT INTO review_tag_maps (review_id, tag_id) VALUES
    (13, 15),
    (14, 21),
    (15, 5);


/* ==================================================
   5. 리뷰 이미지 & 답글
   ================================================== */

-- 리뷰 1번 이미지 (2장)


/* ==================================================
   6. 자동 생성 샘플 (restaurant_id 1~125)
   - 식당당 0~10개 리뷰
   - 리뷰 태그 1~3개 매핑
   - 일부 리뷰에 이미지 1~2장 첨부
   ================================================== */

-- 6-1. 리뷰 자동 생성 (AUTO_INCREMENT 사용)
INSERT INTO reviews (restaurant_id, user_id, receipt_id, rating, content, created_at, updated_at, status)
SELECT
    r.restaurant_id,
    u.user_id AS user_id,
    NULL,
    3 + (s.n % 3) AS rating,
    CONCAT('샘플 리뷰 ', r.restaurant_id, '-', s.n),
    DATE_SUB(NOW(), INTERVAL (s.n + (r.restaurant_id % 7)) DAY),
    DATE_SUB(NOW(), INTERVAL (s.n + (r.restaurant_id % 7)) DAY),
    'PUBLIC'
FROM restaurants r
JOIN (
    SELECT 1 AS n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
    UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10
) s ON s.n <= (r.restaurant_id % 11)
JOIN (SELECT COUNT(*) AS cnt FROM users) user_cnt
JOIN (
    SELECT u.user_id, (@rownum := @rownum + 1) AS rn
    FROM users u
    CROSS JOIN (SELECT @rownum := 0) vars
    ORDER BY u.user_id
) u
  ON u.rn = ((r.restaurant_id + s.n - 1) % user_cnt.cnt) + 1
WHERE r.restaurant_id BETWEEN 1 AND 125;

-- 6-2. 리뷰 태그 매핑/이미지 생성
-- 아래 블록은 신규로 생성된 리뷰 ID를 선택해 태그/이미지를 매핑합니다.
-- 필요 시 실행 전에 @review_seed_since 값을 기준으로 조정하세요.

TRUNCATE TABLE review_tag_maps;
SET @review_seed_since = DATE_SUB(NOW(), INTERVAL 7 DAY);

INSERT INTO review_tag_maps (review_id, tag_id)
SELECT
    r.review_id,
    ((r.restaurant_id + r.review_id + t.t) % 20) + 1 AS tag_id
FROM reviews r
JOIN (
    SELECT 1 AS t UNION ALL SELECT 2 UNION ALL SELECT 3
) t ON t.t <= ((r.restaurant_id + r.review_id) % 3) + 1
WHERE r.created_at >= @review_seed_since;
