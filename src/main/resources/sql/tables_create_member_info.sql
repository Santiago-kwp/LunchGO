use lunchgo;

CREATE TABLE users (
                       user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       name VARCHAR(50) NOT NULL,
                       nickname VARCHAR(100),
                       phone VARCHAR(20) NOT NULL,
                       birth DATE,
                       gender VARCHAR(10),
                       image VARCHAR(255),
                       company_name VARCHAR(255) NOT NULL,
                       company_address VARCHAR(255) NOT NULL,
                       status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                       created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       last_login_at DATETIME,
                       withdrawal_at DATETIME,
                       marketing_agree TINYINT NOT NULL DEFAULT 0,
                       email_authentication TINYINT NOT NULL DEFAULT 0,
                       role CHAR(20) NOT NULL DEFAULT 'USER',
                       CONSTRAINT chk_user_status
                           CHECK (status IN ('ACTIVE', 'DORMANT', 'WITHDRAWAL'))
);

ALTER TABLE users ALTER COLUMN gender SET DEFAULT 'NONE';

CREATE TABLE owners (
                        owner_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        login_id VARCHAR(50) NOT NULL UNIQUE,
                        password VARCHAR(255) NOT NULL,
                        business_num VARCHAR(30) NOT NULL,
                        name VARCHAR(50) NOT NULL,
                        phone VARCHAR(20) NOT NULL,
                        image VARCHAR(255),
                        status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                        created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        last_login_at DATETIME,
                        role CHAR(20) NOT NULL DEFAULT 'OWNER',
                        start_at date not null,
                        CONSTRAINT chk_owner_status
                            CHECK (status IN ('PENDING', 'ACTIVE', 'WITHDRAWAL'))
);

drop table Managers;

CREATE TABLE managers (
                          manager_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                          login_id VARCHAR(50) NOT NULL UNIQUE,
                          password VARCHAR(255) NOT NULL,
                          role CHAR(20) NOT NULL,
                          last_login_at DATETIME NOT NULL
);

CREATE TABLE specialities (
                              speciality_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                              keyword VARCHAR(50) NOT NULL,
                              is_liked TINYINT NOT NULL DEFAULT 0 -- 0: 싫어함, 1: 좋아함
);

-- 회원관리 부분은 pk, fk 관계가 복잡하지 않기 때문에 바로 관계성 연결
CREATE TABLE staffs(
                       staff_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       name VARCHAR(50) NOT NULL,
                       created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       last_login_at DATETIME,
                       role CHAR(20) NOT NULL DEFAULT 'ROLE_STAFF',
                       owner_id BIGINT NOT NULL,
                       FOREIGN KEY (owner_id) REFERENCES Owners(owner_id)
);

CREATE TABLE Speciality_mappings (
                                     user_id BIGINT NOT NULL,
                                     speciality_id BIGINT NOT NULL,
                                     PRIMARY KEY (user_id, speciality_id),
                                     FOREIGN KEY (user_id) REFERENCES users(user_id),
                                     FOREIGN KEY (speciality_id) REFERENCES specialities(speciality_id)
);

-- 식당 테이블 있어야함
CREATE TABLE restaurants
(
    restaurant_id      BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '식당 ID',
    owner_id           BIGINT       NOT NULL COMMENT '사업자(점주) ID',
    name               VARCHAR(50)  NOT NULL COMMENT '식당명',
    phone              VARCHAR(13)  NOT NULL COMMENT '식당전화번호',
    road_address       VARCHAR(255) NOT NULL COMMENT '도로명주소',
    detail_address     VARCHAR(255) NOT NULL COMMENT '상세주소',
    status             VARCHAR(50) DEFAULT 'OPEN' COMMENT '운영상태',
    description        VARCHAR(255) COMMENT '식당소개문',
    avg_main_price     INT          NOT NULL COMMENT '주메뉴 평균가',
    reservation_limit  INT          NOT NULL COMMENT '예약가능인원 상한',
    holiday_available  TINYINT(1)  DEFAULT 0 COMMENT '공휴일 운영 여부 (0:false, 1:true)',
    preorder_available TINYINT(1)  DEFAULT 0 COMMENT '선주문/선결제 여부 (0:false, 1:true)',
    open_time          TIME         NOT NULL COMMENT '영업시작시간',
    close_time         TIME         NOT NULL COMMENT '영업종료시간',
    open_date          DATE         NOT NULL COMMENT '개업일',
    created_at         DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at         DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',

    CONSTRAINT chk_phone_format CHECK (phone REGEXP '^[0-9]{2,3}-[0-9]{3,4}-[0-9]{4}$'),
    CONSTRAINT chk_restaurant_status CHECK (status IN ('OPEN', 'CLOSED', 'DELETED'))
) COMMENT '식당 정보';

CREATE TABLE bookmarks (
                           bookmark_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                           user_id BIGINT NOT NULL,
                           restaurant_id BIGINT NOT NULL,
                           promotion_agree TINYINT NOT NULL DEFAULT 0,
                           FOREIGN KEY (user_id) REFERENCES Users(user_id)
);

alter table bookmarks add constraint fk_bookmarks_restaurants foreign key (restaurant_id) references restaurants(restaurant_id) on delete cascade ;


