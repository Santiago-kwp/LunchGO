package com.example.LunchGo.reservation.controller;

import com.example.LunchGo.account.dto.CustomUserDetails;
import com.example.LunchGo.reservation.dto.CancelReservationRequest;
import com.example.LunchGo.reservation.dto.CancelReservationResponse;
import com.example.LunchGo.reservation.dto.ReservationCreateRequest;
import com.example.LunchGo.reservation.dto.ReservationCreateResponse;
import com.example.LunchGo.reservation.dto.ReservationHistoryItem;
import com.example.LunchGo.reservation.dto.UserReservationDetailResponse;
import com.example.LunchGo.reservation.dto.UserReservationResponse;
import com.example.LunchGo.reservation.service.ReservationCompletionService;
import com.example.LunchGo.reservation.service.ReservationHistoryService;
import com.example.LunchGo.reservation.service.ReservationService;
import com.example.LunchGo.reservation.service.UserReservationQueryService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;
    private final UserReservationQueryService userReservationQueryService;
    private final ReservationHistoryService reservationHistoryService;
    private final ReservationCompletionService reservationCompletionService;

    // 예약 생성
    @PostMapping
    public ResponseEntity<ReservationCreateResponse> create(@RequestBody ReservationCreateRequest request) {
        try {
            ReservationCreateResponse response = reservationService.create(request);
            if (response == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 슬롯 시간 조회
    @GetMapping("/slots")
    public ResponseEntity<List<LocalTime>> slotTimes(
            @RequestParam Long restaurantId,
            @RequestParam LocalDate slotDate
    ) {
        return ResponseEntity.ok(reservationService.slotTimes(restaurantId, slotDate));
    }

    /**
     * 사용자 예약 내역 (본인 것만)
     * GET /api/reservations/my
     */
    @GetMapping("/my")
    public ResponseEntity<List<UserReservationResponse>> myReservations(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(userReservationQueryService.getMyReservations(userDetails.getId()));
    }

    /**
     * 사용자 예약 상세 (본인 것만)
     * GET /api/reservations/{reservationId}?userId=3
     */
    @GetMapping("/{reservationId}")
    public ResponseEntity<UserReservationDetailResponse> myReservationDetail(
            @PathVariable Long reservationId,
            @RequestParam Long userId
    ) {
        return ResponseEntity.ok(userReservationQueryService.getMyReservationDetail(userId, reservationId));
    }

    /**
     * 사용자 예약 취소
     * POST /api/reservations/{reservationId}/cancel
     */
    @PostMapping("/{reservationId}/cancel")
    public ResponseEntity<CancelReservationResponse> cancel(
            @PathVariable Long reservationId,
            @RequestBody CancelReservationRequest request
    ) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    /**
     * 지난 예약 / 예정 예약 히스토리
     * GET /api/reservations/history?userId=1&type=past
     */
    @GetMapping("/history")
    public ResponseEntity<List<ReservationHistoryItem>> history(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "past") String type
    ) {
        List<ReservationHistoryItem> items = reservationHistoryService.getHistory(userId, type);
        return ResponseEntity.ok(items);
    }

    /**
     * 이용완료 처리
     * PATCH /api/reservations/{reservationId}/complete
     */
    @PatchMapping("/{reservationId}/complete")
    public ResponseEntity<Void> complete(@PathVariable Long reservationId) {
        reservationCompletionService.complete(reservationId);
        return ResponseEntity.noContent().build();
    }
}
