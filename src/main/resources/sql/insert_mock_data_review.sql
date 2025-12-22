/* ==================================================
   1. 기초 데이터 생성 (사용자, 식당, 예약)
   - 리뷰 생성을 위한 필수 선행 데이터입니다.
   ================================================== */

-- 1-1. 사용자 (Users) 생성
-- 1번: 식당 사장님, 2~4번: 일반 직장인 유저
INSERT INTO users (user_id, email, password, name, nickname, phone, role, status, marketing_agree, email_authentication) VALUES
                                                                                                                             (1, 'owner@test.com', 'pass1234', '김사장', '대박나자', '010-1111-1111', 'ROLE_OWNER', 'ACTIVE', 1, 1),
                                                                                                                             (2, 'user1@test.com', 'pass1234', '이대리', '맛집탐방러', '010-2222-2222', 'ROLE_USER', 'ACTIVE', 1, 1),
                                                                                                                             (3, 'user2@test.com', 'pass1234', '박과장', '회식요정', '010-3333-3333', 'ROLE_USER', 'ACTIVE', 1, 1),
                                                                                                                             (4, 'user3@test.com', 'pass1234', '최신입', '막내온탑', '010-4444-4444', 'ROLE_USER', 'ACTIVE', 0, 1);

-- 1-2. 식당 (Restaurants) 생성
-- 1번 유저(김사장)가 소유한 식당
INSERT INTO restaurants (restaurant_id, owner_id, name, phone, road_address, detail_address, status, description, avg_main_price, reservation_limit, open_time, close_time, open_date) VALUES
                                                                                                                                                                                           (1, 1, '숙성도 강남점', '02-123-4567', '서울 강남구 테헤란로 123', '1층 101호', 'OPEN', '최고급 숙성 돼지고기 전문점입니다.', 18000, 50, '11:00:00', '22:00:00', '2023-01-01'),
                                                                                                                                                                                           (2, 1, '바다향기 횟집', '02-987-6543', '서울 강남구 역삼로 456', '2층', 'OPEN', '신선한 제철 회 전문', 35000, 30, '15:00:00', '23:00:00', '2023-02-01');

-- 1-3. 예약 (Reservations) 생성
-- 리뷰 및 영수증 연결을 위한 지난 예약 데이터 (COMPLETED 상태)
INSERT INTO reservations (reservation_id, restaurant_id, owner_id, type, status, reserved_date, reserved_time, party_size, request_message, visit_confirm_status, assumed_deposit_amount, total_prepay_amount, created_at, updated_at) VALUES
                                                                                                                                                                                                                                           (1, 1, 1, 'DINING', 'COMPLETED', DATE_SUB(CURDATE(), INTERVAL 3 DAY), CURDATE(), 4, '조용한 자리로 부탁드립니다.', 'CONFIRMED', 40000, 0, NOW(), NOW()),
                                                                                                                                                                                                                                           (2, 1, 1, 'DINING', 'COMPLETED', DATE_SUB(CURDATE(), INTERVAL 2 DAY), CURDATE(), 8, '회식입니다. 세팅 미리 부탁드려요.', 'CONFIRMED', 80000, 0, NOW(), NOW());

/* ==================================================
   2. 영수증 및 메뉴 데이터 (Receipts, Items)
   - 영수증 인증 리뷰 테스트용
   ================================================== */

-- 2-1. 영수증 (Receipts) 생성
-- 예약 1번에 대한 영수증
INSERT INTO receipts (receipt_id, reservation_id, confirmed_amount, image_url, created_at) VALUES
    (1, 1, 120000, 'https://s3-bucket.com/receipts/receipt_001.jpg', DATE_SUB(NOW(), INTERVAL 2 DAY));

-- 2-2. 영수증 메뉴 상세 (Receipt Items) 생성
INSERT INTO receipt_items (receipt_item_id, receipt_id, menu_name, qty, unit_price, total_price) VALUES
                                                                                                     (1, 1, '숙성 삼겹살', 4, 18000, 72000),
                                                                                                     (2, 1, '김치찌개', 1, 8000, 8000),
                                                                                                     (3, 1, '소주', 4, 5000, 20000),
                                                                                                     (4, 1, '맥주', 4, 5000, 20000);

/* ==================================================
   3. 리뷰 관련 기초 데이터 (Tags)
   ================================================== */

-- 3-1. 리뷰 태그 (Review Tags) 생성
INSERT INTO review_tags (tag_id, name, category, tag_type) VALUES
                                                               (1, '음식이 맛있어요', 'TASTE', 'USER'),
                                                               (2, '재료가 신선해요', 'TASTE', 'USER'),
                                                               (3, '친절해요', 'SERVICE', 'USER'),
                                                               (4, '매장이 넓어요', 'ATMOSPHERE', 'USER'),
                                                               (5, '회식하기 좋아요', 'EFFICIENT', 'USER'),
                                                               (6, '가성비가 좋아요', 'EFFICIENT', 'USER'),
                                                               (7, '좀 시끄러워요', 'ATMOSPHERE', 'USER');

/* ==================================================
   4. 리뷰 데이터 (Reviews, Images, Maps, Comments)
   ================================================== */

-- 4-1. 리뷰 (Reviews) 생성
-- Case A: 영수증 인증된 긍정 리뷰 (이대리 -> 숙성도 강남점)
INSERT INTO reviews (review_id, restaurant_id, user_id, receipt_id, rating, content, created_at, updated_at, status) VALUES
    (1, 1, 2, 1, 5, '팀 회식으로 갔는데 고기 질이 정말 좋네요! 사장님도 친절하시고 룸이 있어서 회식하기 딱 좋습니다. 재방문 의사 100%입니다.', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), 'PUBLIC');

-- Case B: 영수증 없는 일반 리뷰 (박과장 -> 숙성도 강남점)
INSERT INTO reviews (review_id, restaurant_id, user_id, receipt_id, rating, content, created_at, updated_at, status) VALUES
    (2, 1, 3, NULL, 4, '맛은 있는데 저녁 시간대라 웨이팅이 좀 있었어요. 그래도 기다린 보람이 있는 맛입니다.', NOW(), NOW(), 'PUBLIC');

-- Case C: 사진 포함 리뷰 (최신입 -> 바다향기 횟집, 예약 데이터 없이 리뷰만 남긴 케이스 가정 혹은 단순 방문)
-- *참고: FK 제약상 예약/영수증 없이도 리뷰 테이블은 작성이 가능하도록 설계되어 있음 (receipt_id NULL 허용)
INSERT INTO reviews (review_id, restaurant_id, user_id, receipt_id, rating, content, created_at, updated_at, status) VALUES
    (3, 2, 4, NULL, 3, '회는 신선한데 가격대가 좀 있네요. 법카 쓸 때만 올 수 있을 듯...', NOW(), NOW(), 'PUBLIC');


-- 4-2. 리뷰 태그 매핑 (Review Tag Maps)
-- 리뷰 1번: 맛, 친절, 회식 태그
INSERT INTO review_tag_maps (review_id, tag_id) VALUES
                                                    (1, 1), (1, 3), (1, 5);

-- 리뷰 2번: 맛, 시끄러움
INSERT INTO review_tag_maps (review_id, tag_id) VALUES
                                                    (2, 1), (2, 7);

-- 리뷰 3번: 신선함
INSERT INTO review_tag_maps (review_id, tag_id) VALUES
    (3, 2);


-- 4-3. 리뷰 이미지 (Review Images)
-- 리뷰 1번에 사진 2장
INSERT INTO review_images (image_id, review_id, image_url, sort_order) VALUES
                                                                           (1, 1, 'https://s3-bucket.com/reviews/pork_belly_grilled.jpg', 1),
                                                                           (2, 1, 'https://s3-bucket.com/reviews/team_cheers.jpg', 2);

-- 리뷰 3번에 사진 1장
INSERT INTO review_images (image_id, review_id, image_url, sort_order) VALUES
    (3, 3, 'https://s3-bucket.com/reviews/sashimi_platter.jpg', 1);


-- 4-4. 사장님 답글 (Comments)
-- 리뷰 1번(칭찬 리뷰)에 대한 사장님 답글
INSERT INTO comments (comment_id, review_id, content, writer_type, created_at) VALUES
    (1, 1, '이대리님 소중한 리뷰 감사합니다! 다음 회식 때도 미리 예약 주시면 더 좋은 서비스로 보답하겠습니다. ^^', 'OWNER', NOW());

/* ==================================================
   5. 구내식당 메뉴 (Cafeteria Menus) - 보너스
   ================================================== */
INSERT INTO cafeteria_menus (cafeteria_menu_id, user_id, served_date, main_menu_names, raw_text, image_url) VALUES
    (1, 2, CURDATE(), '["제육볶음", "계란말이", "미역국"]', '오늘의 점심: 제육볶음, 계란말이, 미역국, 김치, 쌀밥', 'https://s3-bucket.com/cafeteria/today_lunch.jpg');