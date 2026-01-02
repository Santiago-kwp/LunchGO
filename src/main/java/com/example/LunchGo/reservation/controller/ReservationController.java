package com.example.LunchGo.reservation.controller;

import com.example.LunchGo.reservation.dto.ReservationCreateRequest;
import com.example.LunchGo.reservation.dto.ReservationCreateResponse;
import com.example.LunchGo.reservation.dto.ReservationHistoryItem;
import com.example.LunchGo.reservation.service.ReservationCompletionService;
import com.example.LunchGo.reservation.service.ReservationHistoryService;
import com.example.LunchGo.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;
    private final ReservationHistoryService reservationHistoryService;
    private final ReservationCompletionService reservationCompletionService;

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

    @GetMapping("/slots")
    public ResponseEntity<List<LocalTime>> slotTimes(
            @RequestParam Long restaurantId,
            @RequestParam LocalDate slotDate
    ) {
        List<LocalTime> times = reservationService.slotTimes(restaurantId, slotDate);
        return ResponseEntity.ok(times);
    }

    @GetMapping("/history")
    public ResponseEntity<List<ReservationHistoryItem>> history(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "past") String type
    ) {
        List<ReservationHistoryItem> items = reservationHistoryService.getHistory(userId, type);
        return ResponseEntity.ok(items);
    }

    @PatchMapping("/{reservationId}/complete")
    public ResponseEntity<Void> complete(@PathVariable Long reservationId) {
        reservationCompletionService.complete(reservationId);
        return ResponseEntity.noContent().build();
    }
}
