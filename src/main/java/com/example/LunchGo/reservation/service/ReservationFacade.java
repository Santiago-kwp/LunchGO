package com.example.LunchGo.reservation.service;

import com.example.LunchGo.common.util.RedisUtil;
import com.example.LunchGo.reservation.domain.ReservationType;
import com.example.LunchGo.reservation.domain.VisitStatus;
import com.example.LunchGo.reservation.dto.MenuSnapshot;
import com.example.LunchGo.reservation.dto.ReservationCreateRequest;
import com.example.LunchGo.reservation.dto.ReservationCreateResponse;
import com.example.LunchGo.restaurant.entity.Menu;
import com.example.LunchGo.restaurant.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationFacade {

    // 예약금 관련 상수 (ServiceImpl에서 이동)
    private static final int DEPOSIT_PER_PERSON_DEFAULT = 5000;
    private static final int DEPOSIT_PER_PERSON_LARGE = 10000;
    private static final int DEPOSIT_LARGE_THRESHOLD = 7;

    private final ReservationService reservationService; // 수정된 ReservationService (락 핵심 로직 담당)
    private final MenuRepository menuRepository; // 메뉴 정보 조회 (선주문 처리용)
    private final RedisUtil redisUtil; // 후처리: Redis 저장용

    /**
     * Facade 패턴의 새로운 예약 생성 진입점.
     * 예약 생성의 전체적인 흐름을 관리하며, 락이 필요 없는 로직은 여기서 처리하고
     * 락이 필요한 핵심 로직은 ReservationServiceImpl에 위임합니다.
     *
     * @param request 예약 생성 요청 DTO
     * @return 예약 생성 응답 DTO
     */
    @Transactional // Facade 전체 로직에 대한 트랜잭션 관리
    public ReservationCreateResponse createReservation(ReservationCreateRequest request) {
        // 1. 전처리 단계: 락이 필요 없는 로직 (ReservationServiceImpl에서 이동)
        //    핵심 락이 걸리기 전에 최대한 많은 작업을 처리하여 락 점유 시간을 단축합니다.

        // 요청 유효성 검증 (ServiceImpl에서 이동)
        validate(request);

        // 선주문/선결제 메뉴 처리 및 금액 계산 (ServiceImpl에서 이동)
        List<MenuSnapshot> menuSnapshots = new ArrayList<>();
        Integer precalculatedPrepaySum = null;
        if (ReservationType.PREORDER_PREPAY.equals(request.getReservationType())) {
            int sum = 0;
            for (ReservationCreateRequest.MenuItem mi : request.getMenuItems()) {
                if (mi == null || mi.getMenuId() == null) {
                    throw new IllegalArgumentException("menuId is required");
                }
                if (mi.getQuantity() == null || mi.getQuantity() <= 0) {
                    throw new IllegalArgumentException("quantity must be positive");
                }

                // 메뉴 정보 조회 (ServiceImpl에서 이동)
                Menu menu = menuRepository
                        .findByMenuIdAndRestaurantIdAndIsDeletedFalse(mi.getMenuId(), request.getRestaurantId())
                        .orElseThrow(() -> new IllegalArgumentException("menu not found: " + mi.getMenuId()));

                int unitPrice = menu.getPrice() == null ? 0 : menu.getPrice();
                int qty = mi.getQuantity();
                int lineAmount = unitPrice * qty;
                sum += lineAmount;

                menuSnapshots.add(new MenuSnapshot(menu.getMenuId(), menu.getName(), unitPrice, qty, lineAmount));
            }
            precalculatedPrepaySum = sum;
        }

        // 예약금 계산 (ServiceImpl에서 이동)
        Integer precalculatedDepositAmount = null;
        if (ReservationType.RESERVATION_DEPOSIT.equals(request.getReservationType())) {
            int perPerson = request.getPartySize() >= DEPOSIT_LARGE_THRESHOLD
                    ? DEPOSIT_PER_PERSON_LARGE
                    : DEPOSIT_PER_PERSON_DEFAULT;
            precalculatedDepositAmount = perPerson * request.getPartySize();
        }

        // 2. 핵심 로직 호출 단계: 락이 필요한 로직은 ServiceImpl에 위임
        //    락이 걸린 ServiceImpl의 메서드를 호출하여, 동시성 제어가 필요한 DB 작업을 수행합니다.
        ReservationCreateResponse response = reservationService.createReservationLocked(
                request,
                precalculatedPrepaySum,
                menuSnapshots,
                precalculatedDepositAmount
        );

        // 3. 후처리 단계: 락 해제 후 실행될 로직 (ServiceImpl에서 이동)
        //    메인 트랜잭션이 커밋된 후에 수행되어도 무방한 작업을 처리합니다.
        //    (예: Redis 저장, 알림 발송 등 - 현재는 Redis 저장만 존재)

        // redis에 응답대기로 방문 확정 관련 상태 넣어놓기 (ServiceImpl에서 이동)
        LocalDateTime slotDateTime = LocalDateTime.of(request.getSlotDate(), request.getSlotTime());
        long ttlMillis = Duration.between(LocalDateTime.now(), slotDateTime).toMillis();
        if (ttlMillis < 0) {
            // 이 상황은 이미 validate에서 걸러지거나, 시간적으로 불가능한 경우이나 방어 코드
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reservation slot time is in the past.");
        }
        redisUtil.setDataExpire(String.valueOf(response.getReservationId()), VisitStatus.PENDING.name(), ttlMillis);

        return response;
    }

    /**
     * 예약 요청 유효성 검증 메서드. (ServiceImpl에서 이동)
     * 락이 걸리기 전에 호출되어 기본적인 요청 데이터의 유효성을 확인합니다.
     * @param request 예약 생성 요청 DTO
     */
    private static void validate(ReservationCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request is null");
        }
        if (request.getUserId() == null) {
            throw new IllegalArgumentException("userId is required");
        }
        if (request.getRestaurantId() == null) {
            throw new IllegalArgumentException("restaurantId is required");
        }
        if (request.getSlotDate() == null) {
            throw new IllegalArgumentException("slotDate is required");
        }
        if (request.getSlotTime() == null) {
            throw new IllegalArgumentException("slotTime is required");
        }

        if (request.getPartySize() == null || request.getPartySize() <= 0) {
            throw new IllegalArgumentException("partySize must be positive");
        }

        if (request.getReservationType() == null) {
            throw new IllegalArgumentException("reservationType is required");
        }

        if (ReservationType.PREORDER_PREPAY.equals(request.getReservationType())) {
            if (request.getMenuItems() == null || request.getMenuItems().isEmpty()) {
                throw new IllegalArgumentException("menuItems is required for preorder");
            }
            if (request.getTotalAmount() == null || request.getTotalAmount() <= 0) {
                // 프론트가 보내는 값이지만, 일단 기존 로직 유지 (검증만)
                throw new IllegalArgumentException("totalAmount is required for preorder");
            }
        }

        if (request.getRequestMessage() != null && request.getRequestMessage().length() > 50) {
            throw new IllegalArgumentException("requestMessage must be <= 50 chars");
        }
    }
}