package com.example.LunchGo.restaurant.controller;


import com.example.LunchGo.restaurant.dto.RestaurantCreateRequest;
import com.example.LunchGo.restaurant.dto.RestaurantDetailResponse;
import com.example.LunchGo.restaurant.dto.RestaurantUpdateRequest;
import com.example.LunchGo.restaurant.service.BusinessRestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/business/restaurants")
@RequiredArgsConstructor
public class BusinessRestaurantController {

    private final BusinessRestaurantService businessRestaurantService;

    /**
     * 사업자용 식당 상세 정보 조회 API
     *
     * @param id 식당 ID
     * @return RestaurantDetailResponse DTO를 포함하는 ResponseEntity
     */
    @GetMapping("/{id}")
    public ResponseEntity<RestaurantDetailResponse> getRestaurantDetail(
            @PathVariable("id") Long id
    ) {
        RestaurantDetailResponse response = businessRestaurantService.getRestaurantDetail(id);

        return ResponseEntity.ok(response);
    }

    /**
     * 사업자용 식당 정보 등록 API
     *
     * @param request RestaurantCreateRequest DTO
     * @return 생성된 식당 ID를 포함하는 ResponseEntity
     */
    @PostMapping
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
    @PutMapping("/{id}")
    public ResponseEntity<RestaurantDetailResponse> updateRestaurant(
            @PathVariable("id") Long id,
            @RequestBody RestaurantUpdateRequest request) {
        RestaurantDetailResponse updatedRestaurant = businessRestaurantService.updateRestaurant(id, request);

        return ResponseEntity.ok(updatedRestaurant);
    }

}
