package com.example.LunchGo.restaurant.controller;

import jakarta.servlet.http.HttpServletRequest;
import com.example.LunchGo.restaurant.dto.RestaurantCreateRequest;
import com.example.LunchGo.restaurant.dto.RestaurantDetailResponse;
import com.example.LunchGo.restaurant.dto.RestaurantUpdateRequest;
import com.example.LunchGo.restaurant.service.BusinessRestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BusinessRestaurantController {

    private final BusinessRestaurantService businessRestaurantService;

    /**
     * 사업자용 식당 상세 정보 조회 API
     *
     * @param id 식당 ID
     * @return RestaurantDetailResponse DTO를 포함하는 ResponseEntity
     */
    @GetMapping("/business/restaurants/{id}")
    public ResponseEntity<RestaurantDetailResponse> getRestaurantDetail(
            @PathVariable("id") Long id,
            HttpServletRequest request
    ) {
        String userKey = resolveUserKey(request);
        // TODO(restaurant): 이 조회수 증가는 현재 사업자 상세 조회에 임시 적용됨. 사용자용 상세 API 구현 시 이동 필요.
        RestaurantDetailResponse response = businessRestaurantService.getRestaurantDetail(id, userKey);

        return ResponseEntity.ok(response);
    }

    /**
     * 사업자용 식당 정보 등록 API
     *
     * @param request RestaurantCreateRequest DTO
     * @return 생성된 식당 ID를 포함하는 ResponseEntity
     */
    @PostMapping("/business/restaurants")
    public ResponseEntity<Long> createRestaurant(
            @RequestBody RestaurantCreateRequest request
    ) {
        Long newRestaurantId = businessRestaurantService.createRestaurant(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(newRestaurantId);
    }


    /**
     * 사업자용 식당 정보 수정 API
     *
     * @param id      식당 ID
     * @param request RestaurantUpdateRequest DTO
     * @return 수정된 RestaurantDetailResponse DTO를 포함하는 ResponseEntity
     */
    @PutMapping("/business/restaurants/{id}")
    public ResponseEntity<RestaurantDetailResponse> updateRestaurant(
            @PathVariable("id") Long id,
            @RequestBody RestaurantUpdateRequest request
    ) {
        RestaurantDetailResponse updatedRestaurant = businessRestaurantService.updateRestaurant(id, request);

        return ResponseEntity.ok(updatedRestaurant);
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
