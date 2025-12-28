package com.example.LunchGo.restaurant.controller;

import com.example.LunchGo.restaurant.dto.MenuDTO;
import com.example.LunchGo.restaurant.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    /**
     * 특정 식당의 모든 메뉴 리스트를 조회합니다.
     *
     * @param restaurantId 식당 ID
     * @return 메뉴 DTO 리스트를 포함하는 ResponseEntity
     */
    @GetMapping("/restaurants/{restaurantId}/menus")
    public ResponseEntity<List<MenuDTO>> getMenus(@PathVariable Long restaurantId) {
        List<MenuDTO> menus = menuService.getMenusByRestaurant(restaurantId);
        return ResponseEntity.ok(menus);
    }
}
