# 연관관계가 많은 테이블부터 삭제하고 생성, bigint pk auto increment

use lunchgo;
# drop database lunchgo;
# create schema lunchgo collate utf8mb4_general_ci;

# 01. 리뷰 테이블 생성
DROP TABLE IF EXISTS reviews;
CREATE TABLE reviews (
    review_id	bigint	NOT NULL PRIMARY KEY AUTO_INCREMENT,
    restaurant_id	bigint	NOT NULL,
    user_id	bigint	NOT NULL,
    receipt_id	bigint	NULL,
    rating	int	NOT NULL	DEFAULT 5,
    content	text	NULL,
    created_at	datetime	NOT NULL DEFAULT current_timestamp,
    updated_at	datetime	NULL DEFAULT current_timestamp,
    status	varchar(20)	NOT NULL	DEFAULT 'PUBLIC'
);

# 02. 리뷰 태그 테이블 생성
DROP TABLE IF EXISTS review_tags;
CREATE TABLE review_tags (
    tag_id	bigint	NOT NULL PRIMARY KEY AUTO_INCREMENT,
    name	varchar(255)	NOT NULL,
    category	varchar(20)	NULL COMMENT 'EFFICIENT, ATMOSPHERE, TASTE, SERVICE, BLINDED',
    tag_type	varchar(20)	NOT NULL	DEFAULT 'USER' COMMENT 'USER or ADMIN'
);


# 03. 리뷰 태그 매핑 테이블 생성
DROP TABLE IF EXISTS review_tag_maps;
CREATE TABLE review_tag_maps (
    review_id	bigint	NOT NULL,
    tag_id	bigint	NOT NULL
);

# 04. 리뷰 이미지 테이블 생성
DROP TABLE IF EXISTS review_images;
CREATE TABLE review_images (
    image_id	bigint	NOT NULL PRIMARY KEY AUTO_INCREMENT,
    review_id	bigint	NOT NULL,
    image_url	varchar(500)	NOT NULL,
    sort_order	int	NOT NULL	DEFAULT 0
);

# 05. 리뷰 댓글 테이블 생성
DROP TABLE IF EXISTS comments;
CREATE TABLE comments (
    comment_id	bigint	NOT NULL PRIMARY KEY AUTO_INCREMENT,
    review_id	bigint	NOT NULL,
    content	text	NULL,
    writer_type	varchar(20)	NOT NULL	DEFAULT 'OWNER' COMMENT 'OWNER or ADMIN',
    created_at	datetime	NOT NULL DEFAULT current_timestamp,
    updated_at datetime NULL DEFAULT current_timestamp
);

# 06. 영수증 테이블 생성
DROP TABLE IF EXISTS receipts;
CREATE TABLE receipts (
    receipt_id	bigint	NOT NULL PRIMARY KEY AUTO_INCREMENT ,
    reservation_id	bigint	NOT NULL,
    confirmed_amount	int	NOT NULL,
    image_url	varchar(500)	NULL,
    created_at	datetime	NOT NULL DEFAULT current_timestamp
);

# 07. 영수증 메뉴 상세 테이블 생성
DROP TABLE IF EXISTS receipt_items;
CREATE TABLE receipt_items (
    receipt_item_id	bigint	NOT NULL PRIMARY KEY AUTO_INCREMENT,
    receipt_id	bigint	NOT NULL,
    menu_name	varchar(255)	NOT NULL,
    qty	int	NOT NULL,
    unit_price	int	NOT NULL,
    total_price	int	NOT NULL
);

# 08. 구내식당 메뉴 테이블
DROP TABLE IF EXISTS cafeteria_menus;
CREATE TABLE cafeteria_menus (
    cafeteria_menu_id	bigint	NOT NULL PRIMARY KEY AUTO_INCREMENT,
    user_id	bigint	NOT NULL,
    served_date	date	NULL,
    main_menu_names	json	NULL,
    raw_text	text	NULL,
    image_url	varchar(500)	NULL
);



#### 아래는 유닛테스트를 위한 연관 테이블 생성
# 01. 사용자
DROP TABLE IF EXISTS users;
CREATE TABLE users (
    user_id	bigint	NOT NULL PRIMARY KEY AUTO_INCREMENT,
    email	varchar(100)	NOT NULL,
    password	varchar(255)	NOT NULL,
    name	varchar(50)	NOT NULL,
    nickname	varchar(100)	NULL,
    phone	varchar(20)	NOT NULL,
    birth	date	NULL,
    gender	varchar(10)	NULL,
    image	varchar(255)	NULL,
    status	varchar(20)	NOT NULL	DEFAULT 'ACTIVE'	COMMENT 'ACTIVE, DORMANT, WITHDRAWAL',
    created_at	datetime	NOT NULL	DEFAULT current_timestamp,
    updated_at	datetime	NULL	DEFAULT current_timestamp,
    last_login_at	datetime	NULL,
    withdrawal_at	datetime	NULL,
    marketing_agree	tinyint	NOT NULL	DEFAULT 0,
    email_authentication	tinyint	NOT NULL	DEFAULT 0,
    role	char(20)	NOT NULL	DEFAULT 'ROLE_USER',
    company_address	varchar(255)	NULL,
    company_name	varchar(50)	NULL
);

# 02. 식당 테이블
DROP TABLE IF EXISTS restaurants;
CREATE TABLE restaurants (
    restaurant_id	bigint	NOT NULL PRIMARY KEY AUTO_INCREMENT,
    owner_id	bigint	NOT NULL,
    name	varchar(50)	NOT NULL,
    phone	varchar(15)	NOT NULL,
    road_address	varchar(255)	NOT NULL,
    detail_address	varchar(255)	NOT NULL,
    status	varchar(50)	NOT NULL	DEFAULT 'OPEN' COMMENT 'OPEN, CLOSED, DELETED',
    description	text	NULL,
    avg_main_price	int	NOT NULL,
    reservation_limit	int	NOT NULL,
    holiday_available	boolean	NOT NULL	DEFAULT false,
    preorder_available	boolean	NOT NULL	DEFAULT false,
    open_time	time	NOT NULL,
    close_time	time	NOT NULL,
    open_date	date	NOT NULL,
    created_at	datetime	NOT NULL	DEFAULT current_timestamp,
    updated_at	datetime	NOT NULL	DEFAULT current_timestamp
);

# 03. 예약 테이블
DROP TABLE IF EXISTS reservations;
CREATE TABLE reservations
(
    reservation_id           bigint      NOT NULL PRIMARY KEY AUTO_INCREMENT,
    restaurant_id            bigint      NOT NULL,
    owner_id                 bigint      NOT NULL,
    type                     varchar(20) NOT NULL,
    status                   varchar(20) NOT NULL,
    reserved_date            date        NOT NULL,
    reserved_time            date        NOT NULL,
    party_size               int         NOT NULL,
    request_message          text        NULL,
    slot_hold_expires_at     datetime    NULL,
    payment_deadline_at      datetime    NULL,
    visit_confirm_status     varchar(20) NOT NULL,
    visit_confirm_sent_at    datetime    NULL,
    visit_confirm_replied_at datetime    NULL,
    cancelled_at             datetime    NULL,
    cancelled_by             varchar(20) NULL,
    cancel_reason            text        NULL,
    no_show_at               datetime    NULL,
    completed_at             datetime    NULL,
    assumed_deposit_amount   int         NOT NULL,
    total_prepay_amount      int         NOT NULL,
    created_at               datetime    NOT NULL,
    updated_at               datetime    NOT NULL
);



