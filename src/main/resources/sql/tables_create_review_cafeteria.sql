# 01. 리뷰 테이블 생성
DROP TABLE IF EXISTS Reviews;
CREATE TABLE Reviews (
    review_id	bigint	NOT NULL PRIMARY KEY ,
    restaurant_id	bigint	NOT NULL,
    user_id	bigint	NOT NULL,
    receipt_id	bigint	NULL,
    rating	int	NOT NULL	DEFAULT 5,
    content	text	NULL,
    created_at	datetime	NOT NULL,
    updated_at	datetime	NULL,
    status	varchar(20)	NOT NULL	DEFAULT 'PUBLIC'
);

# 02. 리뷰 태그 테이블 생성
DROP TABLE IF EXISTS Review_tags;
CREATE TABLE Review_tags (
    tag_id	bigint	NOT NULL PRIMARY KEY,
    name	varchar(255)	NOT NULL,
    category	varchar(20)	NULL,
    tag_type	varchar(20)	NOT NULL	DEFAULT 'USER'
);


# 03. 리뷰 태그 매핑 테이블 생성
DROP TABLE IF EXISTS Review_tag_maps;
CREATE TABLE Review_tag_maps (
    review_id	bigint	NOT NULL,
    tag_id	bigint	NOT NULL
);

# 04. 리뷰 이미지 테이블 생성
DROP TABLE IF EXISTS Review_images;
CREATE TABLE Review_images (
    image_id	bigint	NOT NULL,
    review_id	bigint	NOT NULL,
    image_url	varchar(500)	NOT NULL,
    sort_order	int	NOT NULL	DEFAULT 0
);

# 05. 리뷰 댓글 테이블 생성
DROP TABLE IF EXISTS Comments;
CREATE TABLE Comments (
comment_id	bigint	NOT NULL,
review_id	bigint	NOT NULL,
content	text	NULL,
writer_type	varchar(20)	NOT NULL	DEFAULT 'OWNER',
created_at	datetime	NOT NULL
);

# 06. 영수증 테이블 생성
DROP TABLE IF EXISTS Receipts;
CREATE TABLE Receipts (
	receipt_id	bigint	NOT NULL,
	reservation_id	bigint	NOT NULL,
	confirmed_amount	int	NOT NULL,
	image_url	varchar(500)	NULL,
	created_at	datetime	NOT NULL
);

# 07. 영수증 메뉴 상세 테이블 생성
DROP TABLE IF EXISTS Receipt_items;
CREATE TABLE Receipt_items (
    receipt_item_id	bigint	NOT NULL,
    receipt_id	bigint	NOT NULL,
    menu_name	varchar(255)	NOT NULL,
    qty	int	NOT NULL,
    unit_price	int	NOT NULL,
    total_price	int	NOT NULL
);

# 08. 구내식당 메뉴 테이블
CREATE TABLE `Cafeteria_menus` (
                                   `cafeteria_menu_id`	bigint	NOT NULL,
                                   `user_id`	bigint	NOT NULL,
                                   `served_date`	date	NULL,
                                   `main_menu_names`	json	NULL	DEFAULT OCR 파싱결과,
                                   `raw_text`	text	NULL,
                                   `image_url`	varchar(500)	NULL
);








