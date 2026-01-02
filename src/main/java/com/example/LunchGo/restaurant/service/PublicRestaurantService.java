package com.example.LunchGo.restaurant.service;

import com.example.LunchGo.restaurant.dto.ImageDTO;
import com.example.LunchGo.restaurant.dto.MenuDTO;
import com.example.LunchGo.restaurant.dto.RegularHolidayDTO;
import com.example.LunchGo.restaurant.dto.RestaurantDetailResponse;
import com.example.LunchGo.restaurant.dto.RestaurantTagDTO;
import com.example.LunchGo.restaurant.entity.Restaurant;
import com.example.LunchGo.restaurant.repository.RestaurantRepository;
import com.example.LunchGo.restaurant.stats.RestaurantStatsEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PublicRestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final MenuService menuService;
    private final RegularHolidayService regularHolidayService;
    private final RestaurantTagService restaurantTagService;
    private final RestaurantStatsEventService statsEventService;
    private final ModelMapper modelMapper;

    public RestaurantDetailResponse getRestaurantDetail(Long restaurantId, String userKey) {
        // 1. Restaurant 기본 정보 및 이미지 조회 (Fetch Join 활용)
        Restaurant restaurant = restaurantRepository.findByIdWithImages(restaurantId)
                .orElseThrow(() -> new NoSuchElementException("Restaurant not found with id: " + restaurantId));

        if (userKey != null && !userKey.isBlank()) {
            statsEventService.recordView(restaurantId, userKey);
        }

        // 2. 메뉴, 정기 휴무일, 태그 정보는 각 전문 서비스에 위임
        List<MenuDTO> menuDtos = menuService.getMenusByRestaurant(restaurantId);
        List<RegularHolidayDTO> regularHolidayDtos = regularHolidayService.getRegularHolidaysByRestaurant(restaurantId);
        List<RestaurantTagDTO> restaurantTagDtos = restaurantTagService.getTagsByRestaurant(restaurantId);

        // 3. DTO 매핑
        List<ImageDTO> imageDtos = restaurant.getRestaurantImages().stream()
                .map(img -> ImageDTO.builder().imageUrl(img.getImageUrl()).build())
                .collect(Collectors.toList());

        RestaurantDetailResponse response = modelMapper.map(restaurant, RestaurantDetailResponse.class);
        response.setMenus(menuDtos);
        response.setImages(imageDtos);
        response.setTags(restaurantTagDtos);
        response.setRegularHolidays(regularHolidayDtos);
        return response;
    }
}
