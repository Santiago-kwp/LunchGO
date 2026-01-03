package com.example.LunchGo.restaurant.controller;

import com.example.LunchGo.account.dto.CustomUserDetails;
import com.example.LunchGo.restaurant.dto.RestaurantCreateRequest;
import com.example.LunchGo.restaurant.dto.RestaurantDetailResponse;
import com.example.LunchGo.restaurant.dto.RestaurantUpdateRequest;
import com.example.LunchGo.restaurant.service.BusinessRestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/business")
@RequiredArgsConstructor
public class BusinessRestaurantController {

    private final BusinessRestaurantService businessRestaurantService;

    @PreAuthorize("hasAuthority('ROLE_OWNER')")
    @GetMapping("/owner/restaurant")
    public ResponseEntity<?> getRestaurantIdByOwner(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Optional<Long> restaurantIdOpt = businessRestaurantService.findRestaurantIdByOwnerId(userDetails.getId());
        return restaurantIdOpt
                .map(id -> ResponseEntity.ok(Map.of("restaurantId", id)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority('ROLE_OWNER')")
    @GetMapping("/restaurants/{id}")
    public ResponseEntity<RestaurantDetailResponse> getRestaurantDetail(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Optional<Long> restaurantId = businessRestaurantService.findRestaurantIdByOwnerId(userDetails.getId());
        if (restaurantId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        // userKey는 사업자 전용 조회 기능에서 사용하지 않음. 현재는 ownerId로 대체하거나 다른 식별자 사용 가능
        RestaurantDetailResponse response = businessRestaurantService.getRestaurantDetail(id, userDetails.getId());
        return restaurantId.get().equals(id)
                ? ResponseEntity.ok(response)
                : ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @PreAuthorize("hasAuthority('ROLE_OWNER')")
    @PostMapping("/restaurants")
    public ResponseEntity<Long> createRestaurant(
            @RequestBody RestaurantCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long newRestaurantId = businessRestaurantService.createRestaurant(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(newRestaurantId);
    }

    @PreAuthorize("hasAuthority('ROLE_OWNER')")
    @PutMapping("/restaurants/{id}")
    public ResponseEntity<RestaurantDetailResponse> updateRestaurant(
            @PathVariable("id") Long id,
            @RequestBody RestaurantUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Optional<Long> restaurantId = businessRestaurantService.findRestaurantIdByOwnerId(userDetails.getId());
        if (restaurantId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        RestaurantDetailResponse updatedRestaurant = businessRestaurantService.updateRestaurant(id, userDetails.getId(), request);
        return restaurantId.get().equals(id)
                ? ResponseEntity.ok(updatedRestaurant)
                : ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
}
