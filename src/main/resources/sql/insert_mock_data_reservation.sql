use lunchgo;


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
