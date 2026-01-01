package com.example.LunchGo.restaurant.controller;

import com.example.LunchGo.restaurant.dto.RestaurantDetailResponse;
import com.example.LunchGo.restaurant.service.PublicRestaurantService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class PublicRestaurantController {

    private final PublicRestaurantService publicRestaurantService;

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantDetailResponse> getRestaurantDetail(
            @PathVariable("id") Long id,
            HttpServletRequest request
    ) {
        if (id == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        String userKey = resolveUserKey(request);
        RestaurantDetailResponse response = publicRestaurantService.getRestaurantDetail(id, userKey);
        return ResponseEntity.ok(response);
    }

    private String resolveUserKey(HttpServletRequest request) {
        String userId = request.getHeader("X-User-Id");
        if (userId != null && !userId.isBlank()) {
            return "user-" + userId.trim();
        }
        if (request.getSession(false) != null) {
            String sessionId = request.getSession(false).getId();
            if (sessionId != null && !sessionId.isBlank()) {
                return "session-" + sessionId;
            }
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return "ip-" + forwarded.split(",")[0].trim();
        }
        return "ip-" + request.getRemoteAddr();
    }
}
