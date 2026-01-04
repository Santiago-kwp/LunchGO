package com.example.LunchGo.reservation.service;

import com.example.LunchGo.reservation.domain.Reservation;
import com.example.LunchGo.reservation.domain.ReservationSlot;
import com.example.LunchGo.reservation.domain.ReservationStatus;
import com.example.LunchGo.reservation.dto.ReservationCreateRequest;
import com.example.LunchGo.reservation.dto.ReservationCreateResponse;
import com.example.LunchGo.reservation.mapper.ReservationMapper;
import com.example.LunchGo.reservation.mapper.row.ReservationCreateRow;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationServiceImpl implements ReservationService {

    private static final DateTimeFormatter CODE_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final ReservationMapper reservationMapper;
    private final ReservationSlotService reservationSlotService;


    @Override
    @Transactional
    public ReservationCreateResponse create(ReservationCreateRequest request) {
        validate(request);

        // 이 메소드가 끝난 후에도 @Transactional 범위 내에 있으므로 락은 계속 유지(트랜잭션 전파)
        // 지정한 날짜+시간대의 예약슬롯을 불러오는 서비스 로직(없으면 신규 생성)
        ReservationSlot slot = reservationSlotService.getValidatedSlot(
                request.getRestaurantId(),
                request.getSlotDate(),
                request.getSlotTime(),
                request.getPartySize()
        );

        Reservation reservation = new Reservation();
        reservation.setReservationCode("PENDING");
        reservation.setSlotId(slot.getSlotId());
        reservation.setUserId(request.getUserId());
        reservation.setPartySize(request.getPartySize());
        reservation.setReservationType(request.getReservationType());
        reservation.setStatus(ReservationStatus.TEMPORARY);
        reservation.setRequestMessage(trimToNull(request.getRequestMessage()));
        reservation.setHoldExpiresAt(LocalDateTime.now().plusMinutes(7));

        reservationMapper.insertReservation(reservation);

        String code = generateReservationCode(LocalDate.now(), reservation.getReservationId());
        reservationMapper.updateReservationCode(reservation.getReservationId(), code);

        ReservationCreateRow row = reservationMapper.selectReservationCreateRow(reservation.getReservationId());
        if (row == null) {
            // 생성 직후 조회 실패면 데이터 이상이므로 런타임 에러로 바로 터뜨림
            throw new IllegalStateException("created reservation not found");
        }

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
}
