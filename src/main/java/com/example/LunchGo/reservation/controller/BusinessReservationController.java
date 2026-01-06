package com.example.LunchGo.reservation.controller;

import com.example.LunchGo.reservation.dto.BusinessReservationDetailResponse;
import com.example.LunchGo.reservation.dto.BusinessReservationItemResponse;
import com.example.LunchGo.reservation.service.BusinessReservationQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/business/reservations")
public class BusinessReservationController {

    private final BusinessReservationQueryService businessReservationQueryService;

    @GetMapping
    public ResponseEntity<List<BusinessReservationItemResponse>> getList(@RequestParam Long restaurantId) {
        return ResponseEntity.ok(businessReservationQueryService.getList(restaurantId));
    }

    @GetMapping("/{reservationId}")
    public ResponseEntity<BusinessReservationDetailResponse> getDetail(@PathVariable Long reservationId) {
        return ResponseEntity.ok(businessReservationQueryService.getDetail(reservationId));
    }
}
