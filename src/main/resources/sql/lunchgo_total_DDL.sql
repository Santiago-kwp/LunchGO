drop schema lunchgo;
create schema lunchgo collate=utf8mb3_general_ci;

use lunchgo;


create table daily_global_finance
(
    stat_date            date   not null
        primary key,
    revenue_subscription bigint null,
    revenue_matching_fee bigint null,
    revenue_preorder_fee bigint null,
    revenue_penalty      bigint null,
    revenue_total        bigint null,
    float_deposit_total  bigint null comment '당일 마감 기준',
    receivable_total     bigint null comment '당일 마감 기준(연체포함)',
    payment_success_cnt  int    null,
    payment_fail_cnt     int    null
);

create table forbidden_words
(
    word_id    bigint auto_increment comment '금칙어 ID'
        primary key,
    word       varchar(100)                         not null comment '금칙어 원문',
    enabled    tinyint(1) default 1                 not null comment '활성 여부',
    created_at datetime   default CURRENT_TIMESTAMP not null comment '생성일',
    updated_at datetime   default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '수정일',
    constraint uk_forbidden_word
        unique (word)
)
    comment '금칙어 목록' charset = utf8mb4;

create table managers
(
    manager_id    bigint auto_increment
        primary key,
    login_id      varchar(50)  not null,
    password      varchar(255) not null,
    role          char(20)     not null,
    last_login_at datetime     not null,
    constraint login_id
        unique (login_id)
);

create table menu_tag_maps
(
    menu_id bigint not null comment '식당메뉴ID',
    tag_id  bigint not null comment '태그ID',
    primary key (menu_id, tag_id)
)
    comment '식당메뉴-태그 매핑';

create table menus
(
    menu_id       bigint auto_increment comment '식당메뉴ID'
        primary key,
    restaurant_id bigint                     not null comment '식당ID',
    name          varchar(50)                not null comment '식당메뉴명',
    category      varchar(50) default 'MAIN' null comment '식당메뉴종류',
    description   tinytext                   not null,
    price         int                        not null comment '가격(1인분 기준)',
    is_deleted    tinyint(1)  default 0      null comment '삭제여부',
    constraint chk_menu_category
        check (`category` in ('MAIN','SUB','OTHER'))
)
comment '식당 메뉴';

create table menu_images
(
    menu_image_id bigint auto_increment comment '식당메뉴이미지ID'
        primary key,
    menu_id       bigint   not null comment '식당메뉴ID',
    image_url     tinytext null,
    constraint FKqpgn1rya57y74a1ncxnc476mj
        foreign key (menu_id) references menus (menu_id)
)
    comment '식당 메뉴 이미지';

create table owners
(
    owner_id      bigint auto_increment
        primary key,
    login_id      varchar(50)                              not null,
    password      varchar(255)                             not null,
    business_num  varchar(30)                              not null,
    name          varchar(50)                              not null,
    phone         varchar(20)                              not null,
    image         varchar(255)                             null,
    status        varchar(20) default 'PENDING'            not null,
    created_at    datetime    default CURRENT_TIMESTAMP    not null,
    updated_at    datetime    default CURRENT_TIMESTAMP    null on update CURRENT_TIMESTAMP,
    last_login_at datetime                                 null,
    role          enum ('ADMIN', 'OWNER', 'STAFF', 'USER') not null,
    start_at      date                                     not null,
    withdrawal_at datetime(6)                              null,
    constraint login_id
        unique (login_id),
    constraint chk_owner_status
        check (`status` in ('PENDING','ACTIVE','WITHDRAWAL'))
);

create table regular_holidays
(
    reg_holiday_id bigint auto_increment comment '정기휴무일ID'
        primary key,
    restaurant_id  bigint not null comment '식당ID',
    day_of_week    int    not null comment '휴무요일 (1:일 ~ 7:토)',
    constraint chk_day_of_week
        check (`day_of_week` between 1 and 7)
)
    comment '정기 휴무일';

create table restaurants
(
    restaurant_id      bigint auto_increment comment '식당 ID'
        primary key,
    owner_id           bigint                                not null comment '사업자(점주) ID',
    name               varchar(50)                           not null comment '식당명',
    phone              varchar(15)                           not null comment '식당전화번호',
    road_address       varchar(255)                          not null comment '도로명주소',
    detail_address     varchar(255)                          not null comment '상세주소',
    status             varchar(50) default 'OPEN'            null comment '운영상태',
    description        longtext                              null,
    avg_main_price     int                                   not null comment '주메뉴 평균가',
    reservation_limit  int                                   not null comment '예약가능인원 상한',
    holiday_available  tinyint(1)  default 0                 null comment '공휴일 운영 여부 (0:false, 1:true)',
    preorder_available tinyint(1)  default 0                 null comment '선주문/선결제 여부 (0:false, 1:true)',
    open_time          time                                  not null comment '영업시작시간',
    close_time         time                                  not null comment '영업종료시간',
    open_date          date                                  not null comment '개업일',
    created_at         datetime    default CURRENT_TIMESTAMP null comment '생성일시',
    updated_at         datetime    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '수정일시',
    constraint chk_phone_format
        check (regexp_like(`phone`, '^(050[0-9]|[0-9]{2,3})-[0-9]{3,4}-[0-9]{4}$')),
	constraint chk_restaurant_status
		check (`status` in ('OPEN','CLOSED','DELETED'))
)
comment ' 식당 정보 ';

create table daily_restaurant_stats
(
    stat_date           date   not null,
    restaurant_id       bigint not null,
    view_count          int    null,
    try_count           int    null,
    confirm_count       int    null,
    visit_count         int    null,
    defended_noshow_cnt int    null,
    penalty_stl_amt     int    null,
    revenue_total       bigint null,
    primary key (stat_date, restaurant_id),
    constraint FK_daily_restaurant_stats
        foreign key (restaurant_id) references restaurants (restaurant_id)
);

create table invoices
(
    invoice_id        bigint auto_increment
        primary key,
    restaurant_id     bigint                                not null,
    type              varchar(20) default 'MATCHING'        not null comment 'MATCHING or SUBSCRIPTION',
    status            varchar(20) default 'ISSUED'          not null comment 'ISSUED, PAID, OVERDUE, CANCELLED (시스템 오류|프로모션), PARTIAL',
    supply_amount     int                                   not null comment '공급가액',
    vat_amount        int                                   not null comment '공급가액*0.1(소수점은 내림처리)',
    total_amount      int                                   not null comment '공급가액+부가세',
    billing_ym        varchar(7)                            not null comment '2024-06',
    due_date          date                                  not null,
    latest_alert_sent date                                  null comment '독촉알림 날짜',
    paid_at           datetime                              null,
    created_at        datetime    default CURRENT_TIMESTAMP not null,
    updated_at        datetime    default CURRENT_TIMESTAMP null,
    constraint FK_restaurants_invoices
        foreign key (restaurant_id) references restaurants (restaurant_id)
);

create table invoice_payments
(
    pay_id          bigint auto_increment
        primary key,
    invoice_id      bigint                            not null,
    amount          int                               not null,
    paid_at         datetime                          not null,
    method          varchar(20) default 'CREDIT_CARD' not null comment 'CREDIT_CART, ACCOUNT_TRANSFER',
    status          varchar(20) default 'READY'       null comment 'READY, PAID, FAILED',
    transaction_key varchar(100)                      null comment 'PG거래 고유번호',
    constraint FK_invoice_payments
        foreign key (invoice_id) references invoices (invoice_id)
);

create table invoice_snapshot
(
    invoice_id      bigint      not null
        primary key,
    restaurant_id   bigint      null,
    restaurant_name varchar(50) null,
    owner_phone     varchar(20) null comment '조인 제거용 - 바로 문자 발송',
    total_amount    int         null,
    due_date        date        null,
    overdue_days    int         null,
    status          varchar(20) null comment 'ISSUED, PAID, OVERDUE, CANCELLED (시스템 오류|프로모션), PARTIAL',
    constraint FK_invoice_snapshot
        foreign key (invoice_id) references invoices (invoice_id)
);

create table restaurant_images
(
    restaurant_image_id bigint auto_increment comment '식당이미지ID'
        primary key,
    restaurant_id       bigint   not null comment '식당ID',
    image_url           tinytext null,
    constraint FK714rhrkn3odt4ucjohgipd9h4
        foreign key (restaurant_id) references restaurants (restaurant_id)
)
    comment '식당 이미지';

create table restaurant_reservation_slots
(
    slot_id       bigint auto_increment comment '슬롯 PK'
        primary key,
    restaurant_id bigint                             not null comment '식당 ID',
    slot_date     date                               not null comment '예약 날짜',
    slot_time     time                               not null comment '예약 시간',
    max_capacity  int                                not null comment '최대 수용 인원',
    created_at    datetime default CURRENT_TIMESTAMP not null comment '생성일',
    updated_at    datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '수정일',
    constraint uk_slot
        unique (restaurant_id, slot_date, slot_time),
    constraint fk_slot_restaurant
        foreign key (restaurant_id) references restaurants (restaurant_id)
            on delete cascade
)
    comment '예약 슬롯(락 대상 1행)' charset = utf8mb4;

create index idx_slot_lookup
    on restaurant_reservation_slots (restaurant_id, slot_date, slot_time);

create index idx_restaurants_name
    on restaurants (name);

create index idx_restaurants_owner_id
    on restaurants (owner_id);

create table review_tags
(
    tag_id   bigint auto_increment
        primary key,
    name     varchar(255) not null,
    category varchar(255) null,
    tag_type varchar(255) not null
);

create table search_tags
(
    tag_id   bigint auto_increment comment '태그ID'
        primary key,
    content  varchar(50) not null comment '태그 내용',
    category varchar(50) not null comment '태그 카테고리',
    constraint uk_search_tags_content
        unique (content),
    constraint chk_tag_category
        check (`category` in ('MENUTYPE','TABLETYPE','ATMOSPHERE','FACILITY','INGREDIENT')),
	constraint chk_tag_content_length
		check (length(trim(`content`)) > 0)
)
comment '검색태그';

create table restaurant_tag_maps
(
    restaurant_id bigint not null comment '식당ID',
    tag_id        bigint not null comment '태그ID',
    primary key (restaurant_id, tag_id),
    constraint fk_restaurant_tag_map_restaurant
        foreign key (restaurant_id) references restaurants (restaurant_id)
            on delete cascade,
    constraint fk_restaurant_tag_map_tag
        foreign key (tag_id) references search_tags (tag_id)
            on delete cascade
)
    comment '식당-태그 매핑';

create index idx_search_tags_category
    on search_tags (category);

create table settlements
(
    settlement_id  bigint auto_increment
        primary key,
    restaurant_id  bigint                                not null,
    type           varchar(20) default 'PRE_ORDER'       not null comment 'PRE_ORDER or PENALTY(위약금)',
    payout_date    datetime                              null comment '실제 입금완료일',
    payment_amt    int                                   null comment '고객 결제액(포인트활용 후)',
    pg_fee         int                                   null comment '3.3% PG사 수수료',
    platform_fee   int                                   not null comment '3% 플랫폼 수익',
    vat_on_fee     int                                   not null comment '플랫폼 수익 부가세',
    stl_amount     int                                   not null comment '식당 실입금액',
    status         varchar(20) default 'SCHEDULED'       not null comment 'SCHEDULED, PROCESSING, COMPLETED, FAILED, ON_HOLD',
    scheduled_date date                                  not null,
    created_at     datetime    default CURRENT_TIMESTAMP not null,
    updated_at     datetime    default CURRENT_TIMESTAMP not null,
    constraint FK_restaurants_settlements
        foreign key (restaurant_id) references restaurants (restaurant_id)
);

create table specialities
(
    speciality_id bigint auto_increment
        primary key,
    keyword       varchar(50)       not null,
    is_liked      tinyint default 0 not null
);

create table staffs
(
    staff_id      bigint auto_increment
        primary key,
    email         varchar(100)                             not null,
    password      varchar(255)                             not null,
    name          varchar(50)                              not null,
    created_at    datetime default CURRENT_TIMESTAMP       not null,
    last_login_at datetime                                 null,
    role          enum ('ADMIN', 'OWNER', 'STAFF', 'USER') not null,
    owner_id      bigint                                   not null,
    constraint email
        unique (email),
    constraint fk_staffs_owners
        foreign key (owner_id) references owners (owner_id)
);

create table tag_maps
(
    tag_map_id   bigint auto_increment comment '매핑ID'
        primary key,
    tag_id       bigint  not null comment '식당태그ID',
    specialty_id bigint  not null comment '즐겨찾기ID',
    weight       tinyint not null comment '선호도'
);

create table temporary_holidays
(
    temp_holiday_id bigint auto_increment comment '임시휴무ID'
        primary key,
    restaurant_id   bigint       not null comment '식당ID',
    start_date      date         not null comment '임시휴무시작일자',
    end_date        date         not null comment '임시휴무종료일자',
    reason          varchar(255) not null comment '휴업사유'
)
    comment '임시 휴무일';

create table users
(
    user_id              bigint auto_increment
        primary key,
    email                varchar(100)                             not null,
    password             varchar(255)                             not null,
    name                 varchar(50)                              not null,
    nickname             varchar(100)                             null,
    phone                varchar(20)                              not null,
    birth                date                                     null,
    gender               varchar(10) default 'NONE'               null,
    image                varchar(255)                             null,
    company_name         varchar(255)                             not null,
    company_address      varchar(255)                             not null,
    status               varchar(20) default 'ACTIVE'             not null,
    created_at           datetime    default CURRENT_TIMESTAMP    not null,
    updated_at           datetime    default CURRENT_TIMESTAMP    null on update CURRENT_TIMESTAMP,
    last_login_at        datetime                                 null,
    withdrawal_at        datetime                                 null,
    marketing_agree      bit                                      not null,
    email_authentication bit                                      not null,
    role                 enum ('ADMIN', 'OWNER', 'STAFF', 'USER') not null,
    constraint email
        unique (email),
    constraint chk_user_status
        check (`status` in ('ACTIVE','DORMANT','WITHDRAWAL'))
);

create table bookmark_links
(
    link_id      bigint auto_increment
        primary key,
    requester_id bigint                                   not null,
    receiver_id  bigint                                   not null,
    pair_min     bigint as (least(`requester_id`, `receiver_id`)) stored,
    pair_max     bigint as (greatest(`requester_id`, `receiver_id`)) stored,
    status       enum ('APPROVED', 'PENDING', 'REJECTED') not null,
    created_at   datetime default CURRENT_TIMESTAMP       not null,
    responded_at datetime                                 null,
    constraint uq_bookmark_links_pair
        unique (pair_min, pair_max),
    constraint bookmark_links_ibfk_1
        foreign key (requester_id) references users (user_id),
    constraint bookmark_links_ibfk_2
        foreign key (receiver_id) references users (user_id),
    constraint chk_bookmark_links_self
        check (`requester_id` <> `receiver_id`)
);

create index receiver_id
    on bookmark_links (receiver_id);

create index requester_id
    on bookmark_links (requester_id);

create table bookmarks
(
    bookmark_id     bigint auto_increment
        primary key,
    user_id         bigint not null,
    restaurant_id   bigint not null,
    promotion_agree bit    null,
    is_public       bit    not null,
    constraint fk_bookmarks_restaurants
        foreign key (restaurant_id) references restaurants (restaurant_id)
            on delete cascade,
    constraint fk_bookmarks_users
        foreign key (user_id) references users (user_id)
);

create index idx_bookmarks_user_public
    on bookmarks (user_id, is_public);

create table cafeteria_menus
(
    cafeteria_menu_id bigint auto_increment
        primary key,
    user_id           bigint       not null,
    served_date       date         null,
    main_menu_names   json         null,
    raw_text          text         null,
    image_url         varchar(255) null,
    constraint fk_cafeteria_menus_users
        foreign key (user_id) references users (user_id)
            on update cascade on delete cascade
);

create table reservations
(
    reservation_id      bigint auto_increment comment '예약 PK'
        primary key,
    reservation_code    varchar(32)                           not null comment '예약번호(외부노출, 유니크)',
    slot_id             bigint                                not null comment '슬롯 ID(FK, 락/정원 기준)',
    user_id             bigint                                not null comment '예약자 ID',
    party_size          int                                   not null comment '예약 인원',
    reservation_type    varchar(30)                           not null comment '예약 유형(예: RESERVATION_DEPOSIT, PREORDER_PREPAY)',
    status              varchar(20) default 'TEMPORARY'       not null comment '예약 상태(예: TEMPORARY/CONFIRMED/...)',
    request_message     varchar(50)                           null comment '요청사항(최대 50자)',
    visit_status        varchar(20) default 'PENDING'         not null comment '리마인더 응답 상태(PENDING/CONFIRMED/CANCELLED)',
    reminder_token      varchar(64)                           null,
    reminder_sent_at    datetime                              null comment '리마인더 발송 시각',
    visit_responded_at  datetime                              null comment '방문/취소 응답 시각',
    deposit_amount      int                                   null comment '예약금 스냅샷(원)',
    prepay_amount       int                                   null comment '선결제 합계 스냅샷(원)',
    total_amount        int                                   null comment '총 결제 예정액 스냅샷(원)',
    currency            varchar(3)                            not null,
    hold_expires_at     datetime                              null comment '홀드 만료(now+7분)',
    payment_deadline_at datetime                              null comment '결제 마감(PG요청+10분)',
    created_at          datetime    default CURRENT_TIMESTAMP not null comment '생성일',
    updated_at          datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '수정일',
    constraint uk_reservation_code
        unique (reservation_code),
    constraint uk_reservations_reminder_token
        unique (reminder_token),
    constraint uk_user_slot_active
        unique (user_id, slot_id, (if((`status` in (_utf8mb4'TEMPORARY', _utf8mb4'CONFIRMED', _utf8mb4'PREPAY_CONFIRM',
                                                    _utf8mb4'REFUND_PENDING')), 1, NULL))),
    constraint fk_reservation_slot
        foreign key (slot_id) references restaurant_reservation_slots (slot_id),
    constraint fk_reservation_user
        foreign key (user_id) references users (user_id)
)
    comment '예약(슬롯 기반, 중복 컬럼 없음)' charset = utf8mb4;

create table payments
(
    payment_id      bigint auto_increment comment '결제ID(PK)'
        primary key,
    reservation_id  bigint                                not null comment '예약ID(FK)',
    payment_type    varchar(20)                           not null comment '결제유형(DEPOSIT/PREPAID_FOOD)',
    status          varchar(20) default 'READY'           not null comment '결제상태(READY/REQUESTED/PAID/FAILED/CANCELLED/REFUNDED/PARTIALLY_REFUNDED)',
    method          varchar(20) default 'UNKNOWN'         not null comment '결제수단(CARD/TRANSFER/VIRTUAL_ACCOUNT/UNKNOWN)',
    card_type       varchar(20) default 'UNKNOWN'         not null comment '카드구분(PERSONAL/CORPORATE/UNKNOWN)',
    amount          int                                   not null comment '결제금액(원)',
    currency        varchar(3)                            not null,
    pg_provider     varchar(50)                           not null comment 'PG사',
    pg_payment_key  varchar(128)                          null comment 'PG결제키',
    pg_order_id     varchar(64)                           null comment '주문번호(가맹점 주문번호)',
    merchant_uid    varchar(64)                           null comment '가맹점 주문번호(포트원)',
    imp_uid         varchar(64)                           null comment '포트원 결제 고유번호',
    pg_tid          varchar(64)                           null comment 'PG 거래 ID',
    receipt_url     varchar(255)                          null comment '영수증 URL',
    idempotency_key varchar(64)                           null comment '중복 방지 키',
    requested_at    datetime    default CURRENT_TIMESTAMP not null comment '결제요청시각',
    approved_at     datetime                              null comment '결제승인시각',
    failed_at       datetime                              null comment '결제실패시각',
    cancelled_at    datetime                              null comment '결제취소시각',
    created_at      datetime    default CURRENT_TIMESTAMP not null comment '생성일시',
    updated_at      datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '수정일시',
    constraint uk_payments_idempotency
        unique (idempotency_key),
    constraint uk_payments_imp_uid
        unique (imp_uid),
    constraint uk_payments_merchant_uid
        unique (merchant_uid),
    constraint uk_pg_key
        unique (pg_provider, pg_payment_key),
    constraint uk_reservation_payment_type
        unique (reservation_id, payment_type),
    constraint fk_payment_reservation
        foreign key (reservation_id) references reservations (reservation_id)
            on delete cascade
)
    comment '결제(예약금/선결제)' charset = utf8mb4;

create index idx_payment_reservation
    on payments (reservation_id);

create index idx_payment_status
    on payments (status, created_at);

create table receipts
(
    receipt_id       bigint auto_increment
        primary key,
    reservation_id   bigint                             not null,
    confirmed_amount int                                not null,
    image_url        varchar(255)                       null,
    created_at       datetime default CURRENT_TIMESTAMP not null,
    constraint uk_receipts_reservation
        unique (reservation_id),
    constraint fk_receipts_reservations
        foreign key (reservation_id) references reservations (reservation_id)
            on update cascade on delete cascade
);

create table receipt_items
(
    receipt_item_id bigint auto_increment
        primary key,
    receipt_id      bigint       not null,
    menu_name       varchar(255) not null,
    qty             int          not null,
    unit_price      int          not null,
    total_price     int          not null,
    constraint fk_receipt_items
        foreign key (receipt_id) references receipts (receipt_id)
            on update cascade on delete cascade
);

create table refunds
(
    refund_id      bigint auto_increment comment '환불ID(PK)'
        primary key,
    payment_id     bigint                                not null comment '결제ID(FK)',
    requested_by   varchar(20)                           not null comment '환불요청주체(USER/OWNER/ADMIN/SYSTEM)',
    policy_case    varchar(30)                           not null comment '정책케이스(DAY_BEFORE/SAME_DAY_BEFORE_2H/WITHIN_2H/NOSHOW/OWNER_FORCED/TIMEOUT/CORPORATE_CARD)',
    payment_type   varchar(20)                           not null comment '결제유형 스냅샷(DEPOSIT/PREPAID_FOOD)',
    card_type      varchar(20)                           not null comment '카드구분 스냅샷(PERSONAL/CORPORATE/UNKNOWN)',
    refund_rate    decimal(5, 4)                         not null comment '환불율(1.0000/0.5000/0.2000 등)',
    base_amount    int                                   not null comment '기준금액(원)',
    refund_amount  int                                   not null comment '환불금액(원)',
    penalty_amount int                                   not null comment '위약금(원)',
    status         varchar(20) default 'REQUESTED'       not null comment '환불상태(REQUESTED/SUCCEEDED/FAILED)',
    requested_at   datetime    default CURRENT_TIMESTAMP not null comment '환불요청시각',
    completed_at   datetime                              null comment '환불완료시각',
    pg_refund_key  varchar(128)                          null comment 'PG환불키',
    created_at     datetime    default CURRENT_TIMESTAMP not null comment '생성일시',
    constraint fk_refund_payment
        foreign key (payment_id) references payments (payment_id)
            on delete cascade
)
    comment '환불(정책 계산 스냅샷)' charset = utf8mb4;

create index idx_refund_payment
    on refunds (payment_id, requested_at);

create table reservation_cancellations
(
    cancellation_id bigint auto_increment comment '취소ID(PK)'
        primary key,
    reservation_id  bigint                             not null comment '예약ID(FK)',
    cancelled_by    varchar(20)                        not null comment '취소주체(USER/OWNER/ADMIN/SYSTEM)',
    cancelled_by_id bigint                             null comment '취소주체ID(시스템이면 NULL 가능)',
    reason          varchar(255)                       not null comment '취소사유',
    cancelled_at    datetime default CURRENT_TIMESTAMP not null comment '취소시각',
    constraint uk_cancel_reservation
        unique (reservation_id),
    constraint fk_cancel_reservation
        foreign key (reservation_id) references reservations (reservation_id)
            on delete cascade
)
    comment '예약 취소 이력' charset = utf8mb4;

create table reservation_menu_items
(
    reservation_menu_item_id bigint auto_increment comment '예약-메뉴 항목 PK'
        primary key,
    reservation_id           bigint                             not null comment '예약 ID(FK)',
    menu_id                  bigint                             not null comment '메뉴 ID',
    menu_name                varchar(100)                       not null comment '메뉴명 스냅샷',
    unit_price               int                                not null comment '단가 스냅샷(원)',
    quantity                 int                                not null comment '수량',
    line_amount              int                                not null comment '합계(단가*수량)',
    created_at               datetime default CURRENT_TIMESTAMP not null comment '생성일',
    constraint fk_rmi_menu
        foreign key (menu_id) references menus (menu_id),
    constraint fk_rmi_reservation
        foreign key (reservation_id) references reservations (reservation_id)
            on delete cascade
)
    comment '선결제 주문 메뉴 항목(스냅샷)' charset = utf8mb4;

create index idx_rmi_menu
    on reservation_menu_items (menu_id);

create index idx_rmi_reservation
    on reservation_menu_items (reservation_id);

create table reservation_visit_stats
(
    reservation_id        bigint                             not null comment '예약 ID (PK)'
        primary key,
    user_id               bigint                             not null comment '유저 ID',
    restaurant_id         bigint                             not null comment '식당 ID',
    visit_number          int                                not null comment '해당 예약의 n번째 방문',
    prev_visit_date       date                               null comment '직전 방문일',
    days_since_last_visit int                                null comment '직전 방문과의 일수',
    created_at            datetime default CURRENT_TIMESTAMP not null comment '스냅샷 생성 시각',
    constraint fk_rvs_reservation
        foreign key (reservation_id) references reservations (reservation_id)
            on delete cascade
)
    comment '예약별 방문 스냅샷' charset = utf8mb4;

create index idx_rvs_user_restaurant
    on reservation_visit_stats (user_id, restaurant_id);

create index idx_hold_expire
    on reservations (status, hold_expires_at);

create index idx_payment_deadline
    on reservations (status, payment_deadline_at);

create index idx_reservation_slot_status
    on reservations (slot_id, status);

create index idx_reservation_user
    on reservations (user_id, created_at);

create index idx_reservations_reminder_sent_at
    on reservations (reminder_sent_at);

create index idx_reservations_visit_status
    on reservations (visit_status);

create table restaurant_user_stats
(
    restaurant_id   bigint not null,
    user_id         bigint not null,
    visit_cnt       int    null comment '누적방문횟수',
    total_spend_amt int    null comment '객단가 산출용',
    last_visit_date date   null,
    primary key (restaurant_id, user_id),
    constraint FK_Restaurants_Restaurant_user_stats
        foreign key (restaurant_id) references restaurants (restaurant_id),
    constraint fk_restaurant_user_stats_users
        foreign key (user_id) references users (user_id)
);

create table reviews
(
    review_id            bigint auto_increment
        primary key,
    restaurant_id        bigint                             not null,
    user_id              bigint                             not null,
    receipt_id           bigint                             null,
    rating               int      default 5                 not null,
    content              varchar(255)                       null,
    created_at           datetime default CURRENT_TIMESTAMP not null,
    updated_at           datetime default CURRENT_TIMESTAMP null,
    status               varchar(255)                       not null,
    blind_request_tag_id bigint                             null comment '사업자 신고 태그',
    blind_request_reason varchar(255)                       null,
    blind_requested_at   datetime                           null comment '사업자 신고 시각',
    reservation_id       bigint                             null comment '예약 ID',
    constraint uk_reviews_reservation_user
        unique (reservation_id, user_id),
    constraint FK_reviews_restaurants
        foreign key (restaurant_id) references restaurants (restaurant_id)
            on delete cascade,
    constraint fk_reviews_blind_request_tag
        foreign key (blind_request_tag_id) references review_tags (tag_id),
    constraint fk_reviews_receipts
        foreign key (receipt_id) references receipts (receipt_id),
    constraint fk_reviews_reservation
        foreign key (reservation_id) references reservations (reservation_id)
            on delete set null,
    constraint fk_reviews_users
        foreign key (user_id) references users (user_id)
);

create table comments
(
    comment_id  bigint auto_increment
        primary key,
    review_id   bigint                             not null,
    content     varchar(255)                       null,
    writer_type varchar(255)                       not null,
    created_at  datetime default CURRENT_TIMESTAMP not null,
    updated_at  datetime default CURRENT_TIMESTAMP null,
    constraint fk_comments_reviews
        foreign key (review_id) references reviews (review_id)
            on update cascade on delete cascade
);

create table review_images
(
    image_id   bigint auto_increment
        primary key,
    review_id  bigint        not null,
    image_url  varchar(255)  not null,
    sort_order int default 0 not null,
    constraint fk_reviews
        foreign key (review_id) references reviews (review_id)
            on update cascade on delete cascade
);

create index idx_review_images_review_sort
    on review_images (review_id, sort_order);

create table review_tag_maps
(
    review_id bigint not null,
    tag_id    bigint not null,
    primary key (review_id, tag_id),
    constraint FK_review_tags
        foreign key (tag_id) references review_tags (tag_id)
            on update cascade on delete cascade,
    constraint fk_review_tag_maps_reviews
        foreign key (review_id) references reviews (review_id)
            on update cascade on delete cascade
);

create index idx_review_tag_maps_review_tag
    on review_tag_maps (review_id, tag_id);

create index idx_review_tag_maps_tag_review
    on review_tag_maps (tag_id, review_id);

create index idx_reviews_reservation_id
    on reviews (reservation_id);

create index idx_reviews_restaurant_status_created
    on reviews (restaurant_id, status, created_at, review_id);

create index idx_reviews_restaurant_status_rating_created
    on reviews (restaurant_id, status, rating, created_at, review_id);

create table speciality_mappings
(
    user_id       bigint not null,
    speciality_id bigint not null,
    primary key (user_id, speciality_id),
    constraint fk_speciality_mappings_users
        foreign key (user_id) references users (user_id),
    constraint speciality_mappings_ibfk_2
        foreign key (speciality_id) references specialities (speciality_id)
);

create index speciality_id
    on speciality_mappings (speciality_id);

create table weekly_predictions
(
    restaurant_id   bigint                             not null,
    week_start_date date                               not null,
    weekday         int                                not null,
    expected_min    int                                not null,
    expected_max    int                                not null,
    confidence      varchar(10)                        not null,
    evidence        text                               null,
    created_at      datetime default CURRENT_TIMESTAMP not null,
    primary key (restaurant_id, week_start_date, weekday)
);

create index idx_restaurant_week
    on weekly_predictions (restaurant_id, week_start_date);

create index idx_week_start_date
    on weekly_predictions (week_start_date);

create definer = lunchgo_user@`%` view restaurant_summary_view as
select `r`.`restaurant_id`                    AS `id`,
       `r`.`name`                             AS `name`,
       `r`.`road_address`                     AS `road_address`,
       `r`.`detail_address`                   AS `detail_address`,
       `r`.`avg_main_price`                   AS `price`,
       `img`.`image_url`                      AS `image`,
       coalesce(`review_stats`.`rating`, 0.0) AS `rating`,
       coalesce(`review_stats`.`reviews`, 0)  AS `reviews`,
       coalesce(`cat`.`category`, '기타')       AS `category`
from (((`lunchgo`.`restaurants` `r` left join (select `lunchgo`.`restaurant_images`.`restaurant_id`                                                                                                AS `restaurant_id`,
                                                      `lunchgo`.`restaurant_images`.`image_url`                                                                                                    AS `image_url`,
                                                      row_number() OVER (PARTITION BY `lunchgo`.`restaurant_images`.`restaurant_id` ORDER BY `lunchgo`.`restaurant_images`.`restaurant_image_id` ) AS `rn`
                                               from `lunchgo`.`restaurant_images`) `img`
        on (((`r`.`restaurant_id` = `img`.`restaurant_id`) and (`img`.`rn` = 1)))) left join (select `lunchgo`.`reviews`.`restaurant_id` AS `restaurant_id`,
                                                                                                     avg(`lunchgo`.`reviews`.`rating`)   AS `rating`,
                                                                                                     count(0)                            AS `reviews`
                                                                                              from `lunchgo`.`reviews`
                                                                                              where (`lunchgo`.`reviews`.`status` = 'PUBLIC')
                                                                                              group by `lunchgo`.`reviews`.`restaurant_id`) `review_stats`
       on ((`r`.`restaurant_id` = `review_stats`.`restaurant_id`))) left join (select `rtm`.`restaurant_id` AS `restaurant_id`,
                                                                                      min(`st`.`content`)   AS `category`
                                                                               from (`lunchgo`.`restaurant_tag_maps` `rtm` join `lunchgo`.`search_tags` `st`
                                                                                     on ((`st`.`tag_id` = `rtm`.`tag_id`)))
                                                                               where (`st`.`category` = 'MENUTYPE')
                                                                               group by `rtm`.`restaurant_id`) `cat`
      on ((`r`.`restaurant_id` = `cat`.`restaurant_id`)))
where (`r`.`status` = 'OPEN');

-- comment on column restaurant_summary_view.id not supported: 식당 ID

-- comment on column restaurant_summary_view.name not supported: 식당명

-- comment on column restaurant_summary_view.road_address not supported: 도로명주소

-- comment on column restaurant_summary_view.detail_address not supported: 상세주소

-- comment on column restaurant_summary_view.price not supported: 주메뉴 평균가

create
    definer = lunchgo_user@`%` procedure getRestaurantIdFromOwnerID(IN in_ownerId bigint)
begin
    SELECT u.email
    FROM bookmarks b
             JOIN users u ON b.user_id = u.user_id
             JOIN restaurants r ON b.restaurant_id = r.restaurant_id
    WHERE r.owner_id = in_ownerId
      AND b.promotion_agree = 1 AND u.status='ACTIVE' AND u.marketing_agree = 1;
end;

