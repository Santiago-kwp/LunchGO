# 동시성 제어 - 중복 예약 처리

## 문제 1: 이미 생성된 중복 예약으로 인한 unique 키 생성 불가

### 증상

- reservations 테이블에 user_id, slot_id에 관한 unique 복합 키를 사용하여 중복 예약을 방지하려 했으나, unique 키를 적용할 수 없는 문제가 발생함

### 원인

- 이미 reservations 테이블에 user_id, slot_id가 중복되는 데이터 행들이 존재하여 unique 키를 적용하지 못 함.

### 해결 방안 1 - reservations 테이블의 중복 데이터 삭제(적용)

- reservations 테이블에 이미 저장된 중복 데이터만 찾아서 삭제하는 방식
- reservations 테이블의 중복 데이터를 그대로 삭제한다는 아래의 방안들은 위험부담이 컸음.
  - 방안1) reservations 테이블에 설정된 외래키 참조 비활성화 후 중복 데이터 삭제 
    - 문제점: reservations 테이블을 참조 중인 다수의 테이블들이 존재하여, 단순히 reservations 테이블의 중복된 데이터만을 삭제하면 다른 테이블과의 데이터 정합성에 문제가 생길 가능성이 있음
  - 방안2) reservations 테이블의 fk 키에 `ON DELETE CASCADE` 옵션을 적용한 후 중복 데이터 삭제
    - 문제점: 이 방식을 사용할 경우, reservations 테이블의 중복 데이터뿐만 아니라, 해당 데이터를 참조하는 다른 테이블의 데이터까지 삭제되어 잠재적인 문제가 발생할 가능성이 있음
  - 방안3) 기존 외래키 관계를 유지하면서, reservations 테이블에서 참조하지 않는 중복 데이터를 찾아 삭제
    - 1단계: 아래의 sql 쿼리를 사용하여 reservations 테이블에서 user_id, slot_id가 중복된 데이터를 탐색
      ```
      SELECT r.reservation_id, r.slot_id
      FROM reservations r
        INNER JOIN
        (SELECT user_id, slot_id, IF(status IN ('TEMPORARY', 'CONFIRMED', 'PREPAY_CONFIRM'), 1, NULL) AS is_active, COUNT(*)
         FROM reservations
         GROUP BY user_id, slot_id, is_active
         HAVING COUNT(*) > 1) r2
      on r.user_id = r2.user_id and r.slot_id = r2.slot_id;
      ```
    - 2단계: reservations 테이블의 기본키를 외래키로 사용하는 테이블들을 파악한 후, 찾은 각각의 테이블에서 참조하는 reservation_id를 조회
      ```sql
      SELECT
        TABLE_NAME,
        COLUMN_NAME,
        CONSTRAINT_NAME
      FROM
        INFORMATION_SCHEMA.KEY_COLUMN_USAGE
      WHERE
        REFERENCED_TABLE_NAME = 'reservations'
          AND REFERENCED_TABLE_SCHEMA = DATABASE();
      
      
      select review_id, reservation_id from reviews where reservation_id in (1, 7, 8, 9, 11, 12, 13, 233, 244, 262, 258, 259, 260, 271, 267);
      select payment_id, reservation_id from payments where reservation_id in (1, 7, 8, 9, 11, 12, 13, 233, 244, 262, 258, 259, 260, 271, 267);
      select receipt_id, reservation_id from receipts where reservation_id in (1, 7, 8, 9, 11, 12, 13, 233, 244, 262, 258, 259, 260, 271, 267);
      select cancellation_id, reservation_id from reservation_cancellations where reservation_id in (1, 7, 8, 9, 11, 12, 13, 233, 244, 262, 258, 259, 260, 271, 267);
      select reservation_id from reservation_visit_stats where reservation_id in (1, 7, 8, 9, 11, 12, 13, 233, 244, 262, 258, 259, 260, 271, 267);
      ```
      - 위의 쿼리를 사용하여 외래키로 사용된 reservation_id를 파악한 다음, reservations에서 user_id, slot_id가 중복된 데이터 행 중 아래 조건에 해당하는 행을 삭제
        - reservations 테이블에서 deposit_amount, prepay_amount 속성값이 모두 null인 중복 데이터 삭제
          (다행히 이 기준을 적용하는 것만으로도 unique 제약조건을 적용할 때 문제가 되었던 중복 데이터를 삭제하는 데 성공함)

### 해결 방안 2 - Redis 활용 및 예약에 관한 중복 데이터 방지 로직 추가(1차 방어-적용)

- reservations 테이블에 이미 저장된 중복 데이터는 삭제하지 않고, 향후 동일 예약 요청을 중복 처리하는 것을 방지하는 것에 집중
- 다음의 장점으로 인해, Redis를 활용하면서, 애플리케이션 레벨에서 중복 요청을 처리하는 방향으로 진행
  - DB의 외래키 관계나 중복된 데이터를 건드리지 않으므로, 데이터 삭제로 인한 위험부담이 없음
  - 사용자의 예약 요청 시, Redis를 통해 중복된 예약 내역이 누적되는 것을 방지
  - 기존의 DB 비관적 락과 Redis 락을 모두 적용
    - DB 비관적 락: 예약 인원이 식당의 최대 수용가능인원을 초과하는 것을 방지
    - Redis : 짧은 시간 동안 동일한 예약 요청이 중복 발생하는 것을 방지

### 해결 방안 3 - 조건부 unique 인덱스(2차 방어-적용)

- Redis를 활용하더라도, reservations 테이블에 unique 키를 설정하는 방안 역시 여전히 유효함.
  - Redis : DB 접근 전 애플리케이션 레벨에서 동시에 발생한 중복된 예약 요청을 차단한다는 측면에서 유리
  - unique 키/인덱스 : 1개의 예약 내역을 DB에 추가할 때 데이터 무결성을 검증한다는 측면에서 유리
- 동일한 예약 요청을 중복 처리하는 것을 방지하면서도, 동일한 사용자가 취소 후 다시 예약할 때는 예약이 가능해야 함
  - 조건부 unique 키 사용
    - 이미 예약 중일 때는 동일한 사용자가 동일한 예약 요청을 보낼 수 없지만, 인원수 변동 등으로 인해 기존 예약을 취소하고 다시 예약할 경우에는 예약이 가능해야 함.
    - user_id, slot_id, is_active 속성으로 구성된 복합 키
    - user_id, slot_id는 reservations 테이블을 구성하는 실제 속성이지만, is_active는 reservations의 status 속성값에 의해 값이 결정되는 가상 칼럼
  - is_active(타입: boolean)
    - 사용자가 이미 예약 완료된 상태에서 동일한 예약을 중복 요청하는 것인지, 기존 예약을 취소한 후 다시 예약하는 것인지를 식별하기 위한 가상 칼럼
    - true: reservations 테이블의 status 속성값 중 예약석을 점유하게 되는 예약상태('TEMPORARY', 'CONFIRMED', 'PREPAY_CONFIRM', 'REFUND_PENDING')일 때
    - false: 사용자가 점유 중이던 예약석이 풀리는 나머지 상태('EXPIRED', 'COMPLETED', 'REFUNDED', 'NO_SHOW', 'CANCELED')일 때
  - DB의 가상 컬럼
    - DB 테이블을 구성하는 물리적인 컬럼으로부터 불러오는 값이 아닌, 다른 컬럼들의 값을 토대로 계산된 결과값을 제공하는 컬럼
    - DB 인덱스를 구성하는 속성으로 가상 칼럼을 활용할 수 있음
  
- DB 차원에서는 reservations 테이블에 조건부 unique 인덱스를 사용
  - 새로운 테이블 속성을 추가하여 기존 reservations 테이블 스키마를 변경하지 않고도 가상 칼럼을 활용한 unique 제약 조건을 적용하기 위해 사용
  - (MySQL 8.0.13 버전 이후부터 제공되는 함수 기반 인덱스 문법 사용)
  ```sql
  CREATE UNIQUE INDEX uk_user_slot_active
  ON reservations (
        user_id,
        slot_id,
        (IF(status IN ('TEMPORARY', 'CONFIRMED', 'PREPAY_CONFIRM', 'REFUND_PENDING'), 1, NULL)) -- 수식을 직접 작성
  );
  ```
  - 중복 예약 방지 로직에서 조건부 unique 인덱스가 동작하는 방식 
    - 예약석을 점유하는 상태일 경우(is_active=true): 해당 예약 내역이 오직 1개만 존재하도록 unique 제약조건을 엄격하게 적용
    - 예약석을 점유하지 않는 상태(is_active=NULL)에서는 unique 제약 조건 적용 대상에서 제외 
      - sql 표준에서의 null은 '알 수 없는 값'이란 의미이기 때문에, unique 제약조건에서는 null값의 중복을 허용
      - 따라서, 예약 취소로 인해 is_active의 값이 null이 되면 사용자가 인원수 수정 등의 이유로 예약을 취소하고 다시 예약 신청하는 것이 가능해짐

### 보충 정리: unique 인덱스와 Redis를 함께 활용하는 이유

- 요약: 둘 다 적용하는 것이 최선
- redis 없이 unique 인덱스만 적용할 경우, 모든 예약 요청이 애플리케이션 레벨에서의 필터링 없이 DB에 도달
  - DB 부하 증가로 인해 전체 서비스가 느려지는 등의 성능 문제가 발생할 수 있음
  - 예약 생성 과정에서 오류가 발생한 경우에도 DB의 auto increment id값(주로 pk)은 증가
- redis를 사용하면 애플리케이션 레벨에서 일부 중복 예약 요청을 먼저 차단하게 되어 DB의 부담이 감소
  - 중복 예약 처리와는 별개로, redis는 예약 처리 만료시간을 설정할 때도 활용될 수 있음