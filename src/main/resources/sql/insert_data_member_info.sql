use lunchgo;

USE lunchgo;

-- ==========================================
-- 1. 독립적인 테이블 데이터 생성 (FK 의존성 없음)
-- ==========================================

-- [1-1] Users (사용자)
INSERT INTO users (email, password, name, nickname, phone, birth, gender, company_name, company_address, status, marketing_agree, email_authentication) VALUES
                                                                                                                                                            ('kim@lunch.com', '1234', '김판교', '점심사냥꾼', '010-1234-5678', '1992-05-05', 'M', '카카오뱅크', '경기 성남시 분당구', 'ACTIVE', 1, 1),
                                                                                                                                                            ('lee@lunch.com', '1234', '이강남', '맛집탐험가', '010-9876-5432', '1995-12-25', 'NONE', '네이버', '경기 성남시 분당구', 'ACTIVE', 0, 1),
                                                                                                                                                            ('park@lunch.com', '1234', '박휴면', '잠자는유저', '010-1111-2222', '1988-08-15', 'M', '삼성전자', '서울 서초구', 'DORMANT', 1, 1),
                                                                                                                                                            ('choi@lunch.com', '1234', '최탈퇴', NULL, '010-3333-4444', '1990-01-01', 'F', '무소속', '서울', 'WITHDRAWAL', 0, 0);

-- [1-2] Owners (점주)
INSERT INTO owners (login_id, password, business_num, name, phone, status, role) VALUES
                                                                                     ('owner_korean', '1234', '111-22-33333', '백종원', '010-5555-6666', 'ACTIVE', 'OWNER'),
                                                                                     ('owner_japan', '1234', '444-55-66666', '최현석', '010-7777-8888', 'ACTIVE', 'OWNER'),
                                                                                     ('owner_new', '1234', '777-88-99999', '새내기', '010-0000-0000', 'PENDING', 'OWNER');

-- [1-3] Managers (관리자) - last_login_at NOT NULL 주의
INSERT INTO managers (login_id, password, role, last_login_at) VALUES
                                                                   ('admin_master', '1234', 'SUPER_ADMIN', NOW()),
                                                                   ('admin_sub', '1234', 'CS_MANAGER', NOW());

-- [1-4] Specialities (취향 키워드)
INSERT INTO specialities (keyword, is_liked) VALUES
                                                 ('한식', 1),     -- 1
                                                 ('일식', 1),     -- 2
                                                 ('양식', 1),     -- 3
                                                 ('매운맛', 1),   -- 4
                                                 ('가성비', 1),   -- 5
                                                 ('오이', 0),     -- 6 (싫어함)
                                                 ('고수', 0);     -- 7 (싫어함)


-- ==========================================
-- 2. 참조 테이블 데이터 생성 (FK 의존성 있음)
-- ==========================================

-- [2-1] Speciality_mappings (유저 <-> 취향 매핑)
INSERT INTO speciality_mappings (user_id, speciality_id) VALUES
                                                             (1, 1), -- 김판교: 한식 선호
                                                             (1, 5), -- 김판교: 가성비 선호
                                                             (2, 2), -- 이강남: 일식 선호
                                                             (2, 6); -- 이강남: 오이 싫어함

-- [2-2] Staffs (직원) -> Owner 참조
INSERT INTO staffs (email, password, name, role, owner_id) VALUES
                                                               ('staff1@store.com', '1234', '알바생1', 'ROLE_STAFF', 1),
                                                               ('manager@store.com', '1234', '매니저1', 'ROLE_MANAGER', 1),
                                                               ('chef@sushi.com', '1234', '초밥장인', 'ROLE_CHEF', 2);

-- [2-3] Restaurants (식당) -> Owner 참조
-- phone 제약조건(REGEXP) 주의: 000-0000-0000 형식 준수
INSERT INTO restaurants (
    owner_id, name, phone, road_address, detail_address,
    status, description, avg_main_price, reservation_limit,
    holiday_available, preorder_available, open_time, close_time, open_date
) VALUES
      (
          1, -- owner_korean
          '판교 숯불갈비',
          '031-123-4567',
          '경기도 성남시 분당구 판교역로 123',
          '1층 101호',
          'OPEN',
          '참숯으로 구운 진한 갈비의 맛',
          18000,
          50,
          1, -- 공휴일 영업함
          1, -- 선주문 가능
          '11:00:00',
          '22:00:00',
          '2023-01-01'
      ),
      (
          1, -- owner_korean (다점포 소유 가정)
          '판교 김치찌개',
          '031-123-4568',
          '경기도 성남시 분당구 판교역로 123',
          '지하 102호',
          'OPEN',
          '직장인 점심 해장 추천',
          9000,
          30,
          0, -- 공휴일 쉼
          0, -- 선주문 불가
          '10:30:00',
          '21:00:00',
          '2023-03-15'
      ),
      (
          2, -- owner_japan
          '스시 오마카세 런치',
          '02-555-1234',
          '서울시 강남구 테헤란로 456',
          '2층',
          'OPEN',
          '신선한 제철 생선으로 만드는 스시',
          50000,
          10,
          1,
          1,
          '12:00:00',
          '23:00:00',
          '2024-01-01'
      );

-- [2-4] Bookmarks (즐겨찾기) -> User, Restaurant 참조
INSERT INTO bookmarks (user_id, restaurant_id, promotion_agree) VALUES
                                                                    (1, 1, 1), -- 김판교 -> 판교 숯불갈비 (알림 ON)
                                                                    (1, 3, 0), -- 김판교 -> 스시 오마카세 (알림 OFF)
                                                                    (2, 3, 1), -- 이강남 -> 스시 오마카세 (알림 ON)
                                                                    (3, 2, 0); -- 박휴면 -> 판교 김치찌개 (알림 OFF)