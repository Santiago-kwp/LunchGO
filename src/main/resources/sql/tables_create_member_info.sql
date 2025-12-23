use lunchgo;

DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS owners;
DROP TABLE IF EXISTS reviews;
DROP TABLE IF EXISTS review_tag_maps;
DROP TABLE IF EXISTS receipts;
DROP TABLE IF EXISTS review_tags;
DROP TABLE IF EXISTS comments;
DROP TABLE IF EXISTS receipt_items;
DROP TABLE IF EXISTS review_images;
DROP TABLE IF EXISTS search_tags;

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

drop table if exists Managers;

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


CREATE TABLE bookmarks (
                           bookmark_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                           user_id BIGINT NOT NULL,
                           restaurant_id BIGINT NOT NULL,
                           promotion_agree TINYINT NOT NULL DEFAULT 0,
                           FOREIGN KEY (user_id) REFERENCES Users(user_id)
);


