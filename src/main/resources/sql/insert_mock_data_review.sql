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
TRUNCATE TABLE reservations;
SET FOREIGN_KEY_CHECKS = 1;


/* ==================================================
   1. 기초 데이터 (예약, 영수증)
   ================================================== */

-- 1-1. 예약 (Reservations)
INSERT INTO reservations (reservation_id, restaurant_id, owner_id, type, status, reserved_date, reserved_time, party_size, request_message, visit_confirm_status, assumed_deposit_amount, total_prepay_amount, created_at, updated_at) VALUES
    (1, 1, 1, 'DINING', 'COMPLETED', DATE_SUB(CURDATE(), INTERVAL 3 DAY), CURDATE(), 4, '조용한 자리로 부탁드립니다.', 'CONFIRMED', 40000, 0, NOW(), NOW()),
    (2, 1, 1, 'DINING', 'COMPLETED', DATE_SUB(CURDATE(), INTERVAL 2 DAY), CURDATE(), 8, '회식입니다. 세팅 미리 부탁드려요.', 'CONFIRMED', 80000, 0, NOW(), NOW());

-- 1-2. 영수증 (Receipts)
INSERT INTO receipts (receipt_id, reservation_id, confirmed_amount, image_url, created_at) VALUES
    (1, 1, 120000, 'https://s3-bucket.com/receipts/receipt_001.jpg', DATE_SUB(NOW(), INTERVAL 2 DAY));

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
INSERT INTO review_tags (tag_id, name, category, tag_type) VALUES
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


/* ==================================================
   5. 리뷰 이미지 & 답글
   ================================================== */

-- 리뷰 1번 이미지 (2장)
INSERT INTO review_images (image_id, review_id, image_url, sort_order) VALUES
    (1, 1, 'https://s3-bucket.com/reviews/pork_belly_grilled.jpg', 1),
    (2, 1, 'https://s3-bucket.com/reviews/team_cheers.jpg', 2);

-- 리뷰 3번 이미지 (1장)
INSERT INTO review_images (image_id, review_id, image_url, sort_order) VALUES
    (3, 3, 'https://s3-bucket.com/reviews/sashimi_platter.jpg', 1);

-- 리뷰 1번 답글 (사장님)
INSERT INTO comments (comment_id, review_id, content, writer_type, created_at) VALUES
    (1, 1, '이대리님 소중한 리뷰 감사합니다! 다음 회식 때도 미리 예약 주시면 더 좋은 서비스로 보답하겠습니다. ^^', 'OWNER', NOW());