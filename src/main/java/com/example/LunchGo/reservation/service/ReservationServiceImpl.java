package com.example.LunchGo.reservation.service;

import com.example.LunchGo.common.util.RedisUtil;
import com.example.LunchGo.reservation.domain.*;
import com.example.LunchGo.reservation.dto.ReservationCreateRequest;
import com.example.LunchGo.reservation.dto.ReservationCreateResponse;
import com.example.LunchGo.reservation.exception.DuplicateReservationException;
import com.example.LunchGo.reservation.mapper.ReservationMapper;
import com.example.LunchGo.reservation.mapper.row.ReservationCreateRow;
import com.example.LunchGo.restaurant.entity.Menu;
import com.example.LunchGo.restaurant.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationServiceImpl implements ReservationService {

    private static final DateTimeFormatter CODE_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final int DEPOSIT_PER_PERSON_DEFAULT = 5000;
    private static final int DEPOSIT_PER_PERSON_LARGE = 10000;
    private static final int DEPOSIT_LARGE_THRESHOLD = 7;

    private final ReservationMapper reservationMapper;
    private final ReservationSlotService reservationSlotService;
    private final RedisUtil redisUtil;
    private final MenuRepository menuRepository;

    @Override
    @Transactional
    public ReservationCreateResponse create(ReservationCreateRequest request) {
        validate(request);

        // 지정한 날짜+시간대의 예약슬롯을 불러오는 서비스 로직(없으면 신규 생성)
        ReservationSlot slot = reservationSlotService.getValidatedSlot(
                request.getRestaurantId(),
                request.getSlotDate(),
                request.getSlotTime(),
                request.getPartySize()
        );

        // PREORDER_PREPAY면 메뉴 스냅샷(이름/가격) 확정 + 합계 계산
        List<ReservationCreateRequest.MenuItem> reqMenuItems = request.getMenuItems();
        List<MenuSnapshot> snapshots = new ArrayList<>();
        Integer preorderSum = null;

        if (ReservationType.PREORDER_PREPAY.equals(request.getReservationType())) {
            int sum = 0;
            for (ReservationCreateRequest.MenuItem mi : reqMenuItems) {
                if (mi == null || mi.getMenuId() == null) {
                    throw new IllegalArgumentException("menuId is required");
                }
                if (mi.getQuantity() == null || mi.getQuantity() <= 0) {
                    throw new IllegalArgumentException("quantity must be positive");
                }

                Menu menu = menuRepository
                        .findByMenuIdAndRestaurantIdAndIsDeletedFalse(mi.getMenuId(), request.getRestaurantId())
                        .orElseThrow(() -> new IllegalArgumentException("menu not found: " + mi.getMenuId()));

                int unitPrice = menu.getPrice() == null ? 0 : menu.getPrice();
                int qty = mi.getQuantity();
                int lineAmount = unitPrice * qty;
                sum += lineAmount;

                snapshots.add(new MenuSnapshot(menu.getMenuId(), menu.getName(), unitPrice, qty, lineAmount));
            }
            preorderSum = sum;
        }

        Reservation reservation = new Reservation();
        reservation.setReservationCode("PENDING");
        reservation.setSlotId(slot.getSlotId());
        reservation.setUserId(request.getUserId());
        reservation.setPartySize(request.getPartySize());
        reservation.setReservationType(request.getReservationType());
        reservation.setStatus(ReservationStatus.TEMPORARY);
        reservation.setRequestMessage(trimToNull(request.getRequestMessage()));
        reservation.setHoldExpiresAt(LocalDateTime.now().plusMinutes(7));
        reservation.setVisitStatus(VisitStatus.PENDING);


        if (ReservationType.RESERVATION_DEPOSIT.equals(request.getReservationType())) {
            int perPerson = request.getPartySize() >= DEPOSIT_LARGE_THRESHOLD
                    ? DEPOSIT_PER_PERSON_LARGE
                    : DEPOSIT_PER_PERSON_DEFAULT;
            int depositAmount = perPerson * request.getPartySize();
            reservation.setDepositAmount(depositAmount);
            reservation.setTotalAmount(depositAmount);
        } else if (ReservationType.PREORDER_PREPAY.equals(request.getReservationType())) {
            // 프론트 totalAmount 대신 "DB 메뉴 합계" 기준으로 저장
            reservation.setPrepayAmount(preorderSum);
            reservation.setTotalAmount(preorderSum);
        }

        try {
            reservationMapper.insertReservation(reservation);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateReservationException("이미 결제 대기 중인 예약 요청입니다.");
        }

        // reservation_menu_items 저장 (예약 PK 생긴 다음에)
        if (ReservationType.PREORDER_PREPAY.equals(request.getReservationType()) && !snapshots.isEmpty()) {
            for (MenuSnapshot s : snapshots) {
                reservationMapper.insertReservationMenuItem(
                        reservation.getReservationId(),
                        s.menuId,
                        s.menuName,
                        s.unitPrice,
                        s.quantity,
                        s.lineAmount
                );
            }
        }

        String code = generateReservationCode(LocalDate.now(), reservation.getReservationId());
        reservationMapper.updateReservationCode(reservation.getReservationId(), code);

        ReservationCreateRow row = reservationMapper.selectReservationCreateRow(reservation.getReservationId());
        if (row == null) {
            throw new IllegalStateException("created reservation not found");
        }

        // redis에 응답대기로 방문 확정 관련 상태 넣어놓기
        LocalDateTime slotDateTime = LocalDateTime.of(request.getSlotDate(), request.getSlotTime());
        long ttlMillis = Duration.between(LocalDateTime.now(), slotDateTime).toMillis();
        if (ttlMillis < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        redisUtil.setDataExpire(String.valueOf(reservation.getReservationId()), VisitStatus.PENDING.name(), ttlMillis);

        return new ReservationCreateResponse(
                row.getReservationId(),
                row.getReservationCode(),
                row.getSlotId(),
                row.getUserId(),
                row.getRestaurantId(),
                row.getSlotDate(),
                row.getSlotTime(),
                row.getPartySize(),
                row.getReservationType(),
                row.getStatus(),
                row.getRequestMessage()
        );
    }

    private static void validate(ReservationCreateRequest request) {
        if (request == null) throw new IllegalArgumentException("request is null");
        if (request.getUserId() == null) throw new IllegalArgumentException("userId is required");
        if (request.getRestaurantId() == null) throw new IllegalArgumentException("restaurantId is required");
        if (request.getSlotDate() == null) throw new IllegalArgumentException("slotDate is required");
        if (request.getSlotTime() == null) throw new IllegalArgumentException("slotTime is required");

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

    private static String generateReservationCode(LocalDate today, Long reservationId) {
        String date = today.format(CODE_DATE);
        String seq = String.format("%04d", reservationId);
        return "R" + date + "-" + seq;
    }

    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    @Override
    public List<LocalTime> slotTimes(Long restaurantId, LocalDate slotDate) {
        return reservationMapper.selectSlotTimesByDate(restaurantId, slotDate);
    }

    private static class MenuSnapshot {
        private final Long menuId;
        private final String menuName;
        private final Integer unitPrice;
        private final Integer quantity;
        private final Integer lineAmount;

        private MenuSnapshot(Long menuId, String menuName, Integer unitPrice, Integer quantity, Integer lineAmount) {
            this.menuId = menuId;
            this.menuName = menuName;
            this.unitPrice = unitPrice;
            this.quantity = quantity;
            this.lineAmount = lineAmount;
        }
    }
}
