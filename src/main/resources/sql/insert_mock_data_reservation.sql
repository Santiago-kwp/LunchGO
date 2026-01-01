use lunchgo;


-- 지난 예약 테스트용 슬롯 (user_id=11)
INSERT IGNORE INTO restaurant_reservation_slots
    (slot_id, restaurant_id, slot_date, slot_time, max_capacity)
VALUES
    (1001, 1, '2025-11-15', '11:00:00', 20),
    (1002, 1, '2025-11-10', '12:00:00', 20),
    (1003, 2, '2025-11-16', '19:30:00', 20),
    (1004, 1, '2025-11-20', '12:30:00', 20);

-- 예약 목데이터
INSERT INTO reservations
(reservation_id, reservation_code, slot_id, user_id, party_size, reservation_type, status, request_message,
 hold_expires_at, payment_deadline_at, created_at, updated_at)
VALUES
    (1, 'R20251224-0001', 2, 1, 4, 'RESERVATION_DEPOSIT', 'TEMPORARY', '창가 자리 가능하면 부탁해요',
     '2025-12-24 15:07:25', NULL, '2025-12-24 15:00:25', '2025-12-24 15:00:25'),

    (2, 'R20251224-0002', 1, 2, 6, 'RESERVATION_DEPOSIT', 'CONFIRMED', NULL,
     NULL, NULL, '2025-12-24 13:00:25', '2025-12-24 13:00:25'),

    (3, 'R20251224-0003', 11, 1, 2, 'PREORDER_PREPAY', 'CONFIRMED', '와사비 적게요',
     NULL, NULL, '2025-12-23 15:00:25', '2025-12-23 15:00:25'),

    (4, 'R20251224-0004', 3, 3, 4, 'RESERVATION_DEPOSIT', 'CANCELLED', '조용한 자리',
     '2025-12-24 14:40:25', NULL, '2025-12-24 14:00:25', '2025-12-24 14:30:25'),

    (5, 'R20251224-0005', 5, 4, 5, 'RESERVATION_DEPOSIT', 'COMPLETED', NULL,
     NULL, NULL, '2025-12-21 15:00:25', '2025-12-21 15:00:25'),

    (6, 'R20251224-0006', 6, 2, 4, 'RESERVATION_DEPOSIT', 'NO_SHOW', NULL,
     NULL, NULL, '2025-12-22 15:00:25', '2025-12-22 15:00:25');

-- 지난 예약 테스트용 예약 (user_id=11)
INSERT INTO reservations
    (reservation_id, reservation_code, slot_id, user_id, party_size, reservation_type, status, request_message,
     deposit_amount, prepay_amount, total_amount, currency,
     hold_expires_at, payment_deadline_at, created_at, updated_at)
VALUES
    (101, 'R20251115-0011', 1001, 11, 4, 'RESERVATION_DEPOSIT', 'COMPLETED', NULL,
     15000, NULL, 85000, 'KRW',
     NULL, NULL, '2025-11-15 10:30:00', '2025-11-15 10:30:00'),
    (102, 'R20251110-0011', 1002, 11, 2, 'RESERVATION_DEPOSIT', 'REFUND_PENDING', NULL,
     10000, NULL, 42000, 'KRW',
     NULL, NULL, '2025-11-10 12:00:00', '2025-11-10 12:00:00'),
    (103, 'R20251116-0011', 1003, 11, 3, 'RESERVATION_DEPOSIT', 'REFUNDED', NULL,
     12000, NULL, 95000, 'KRW',
     NULL, NULL, '2025-11-16 19:00:00', '2025-11-16 19:00:00'),
    (104, 'R20251120-0011', 1004, 11, 2, 'PREORDER_PREPAY', 'COMPLETED', NULL,
     NULL, 52000, 52000, 'KRW',
     NULL, NULL, '2025-11-20 12:00:00', '2025-11-20 12:00:00');

-- 선결제 메뉴 샘플 (reservation_id=104)
INSERT INTO reservation_menu_items
    (reservation_menu_item_id, reservation_id, menu_id, menu_name, unit_price, quantity, line_amount, created_at)
VALUES
    (1041, 104, 1, '돈까스 정식', 16000, 1, 16000, '2025-11-20 12:00:00'),
    (1042, 104, 2, '김치나베', 12000, 1, 12000, '2025-11-20 12:00:00'),
    (1043, 104, 3, '우동', 8000, 3, 24000, '2025-11-20 12:00:00');

-- 이용 완료 횟수 통계 (user_id=11)
INSERT INTO restaurant_user_stats
    (restaurant_id, user_id, visit_cnt, total_spend_amt, last_visit_date)
VALUES
    (1, 11, 2, 137000, '2025-11-20');
