package com.example.LunchGo.restaurant.service;

import com.example.LunchGo.restaurant.dto.MenuDTO;
import com.example.LunchGo.restaurant.domain.RestaurantStatus;
import com.example.LunchGo.restaurant.dto.ImageDTO;
import com.example.LunchGo.restaurant.dto.RegularHolidayDTO;
import com.example.LunchGo.restaurant.dto.RestaurantDetailResponse;
import com.example.LunchGo.restaurant.dto.RestaurantCreateRequest; // Import added
import com.example.LunchGo.restaurant.dto.RestaurantTagDTO;
import com.example.LunchGo.restaurant.entity.Restaurant;
import com.example.LunchGo.restaurant.repository.RestaurantRepository;
import com.example.LunchGo.restaurant.stats.RestaurantStatsEventService;
import com.example.LunchGo.restaurant.dto.RestaurantUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BusinessRestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final RegularHolidayRepository regularHolidayRepository;
    // private final RestaurantImageRepository restaurantImageRepository; // Removed
    private final SearchTagRepository searchTagRepository; // Needed for restaurant tags
    private final RestaurantStatsEventService statsEventService;

    private final MenuService menuService;
    private final RegularHolidayService regularHolidayService;
    private final RestaurantTagService restaurantTagService; // SearchTagRepository 대신 RestaurantTagService 주입

    private final ModelMapper modelMapper;

    /**
     * 특정 식당의 상세 정보를 조회합니다.
     *
     * @param restaurantId 식당 ID
     * @return 식당 상세 정보를 담은 RestaurantDetailResponse DTO
     * @throws NoSuchElementException 식당을 찾을 수 없는 경우
     */
    public RestaurantDetailResponse getRestaurantDetail(Long restaurantId) {
        return getRestaurantDetail(restaurantId, null);
    }

    public RestaurantDetailResponse getRestaurantDetail(Long restaurantId, String userKey) {
        // 1. Restaurant 기본 정보 및 이미지 조회 (Fetch Join 활용)
        Restaurant restaurant = restaurantRepository.findByIdWithImages(restaurantId)
                .orElseThrow(() -> new NoSuchElementException("Restaurant not found with id: " + restaurantId));

        if (userKey != null && !userKey.isBlank()) {
            statsEventService.recordView(restaurantId, userKey);
        }

        // 2. RegularHoliday 조회
        List<RegularHoliday> regularHolidays = regularHolidayRepository.findAllByRestaurantId(restaurantId);

        // 3. Restaurant에 매핑된 태그 ID 조회 및 태그 정보 페치
        List<Long> restaurantTagIds = restaurantRepository.findTagIdsByRestaurantId(restaurantId);
        Map<Long, SearchTag> allRestaurantTagsMap = searchTagRepository.findAllById(restaurantTagIds)
                .stream()
                .collect(Collectors.toMap(SearchTag::getTagId, tag -> tag));
        // 2. 메뉴, 정기 휴무일, 태그 정보는 각 전문 서비스에 위임
        List<MenuDTO> menuDtos = menuService.getMenusByRestaurant(restaurantId);
        List<RegularHolidayDTO> regularHolidayDtos = regularHolidayService.getRegularHolidaysByRestaurant(restaurantId);
        List<RestaurantTagDTO> restaurantTagDtos = restaurantTagService.getTagsByRestaurant(restaurantId); // RestaurantTagService에 위임

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


    @Transactional
    public Long createRestaurant(RestaurantCreateRequest request) {
        // 1. Restaurant 엔티티 생성 및 저장
        // TODO: 추후 인증(Authentication) 도입 시 실제 ownerId 사용
        long currentOwnerId = 1L;

        Restaurant restaurant = modelMapper.map(request, Restaurant.class);
        restaurant.setOwnerId(currentOwnerId);
        restaurant.setStatus(RestaurantStatus.OPEN); // 초기 상태는 OPEN

        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        Long newRestaurantId = savedRestaurant.getRestaurantId();

        // 2. 이미지 정보 저장은 ObjectStorage와 연동되어 추후 구현 예정이므로 현재는 건너뜀
        // if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
        //     ...
        // }

        // 3. 식당 메뉴 정보 저장은 MenuService에 위임
        menuService.createMenus(newRestaurantId, request.getMenus());
        
        // 4. 정기 휴무일 정보 저장은 RegularHolidayService에 위임
        regularHolidayService.createRegularHolidaysForRestaurant(newRestaurantId, request.getRegularHolidayNumbers());

        // 5. 태그 정보 저장은 RestaurantTagService에 위임
        restaurantTagService.createTagMappingsForRestaurant(newRestaurantId, request.getSelectedTagIds());

        return newRestaurantId;
    }

    @Transactional
    public RestaurantDetailResponse updateRestaurant(Long id, RestaurantUpdateRequest request) {
        // 1. 식당 정보 조회
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Restaurant not found with id: " + id));

        // 2. 기본 정보 업데이트
        modelMapper.map(request, restaurant);

        // 3. TODO: 식당 이미지 정보 업데이트 (추후 구현)
        // 이미지 파일 처리 로직 (e.g., S3 업로드 및 URL 업데이트) 필요

        // 4. 식당 메뉴 정보 업데이트는 MenuService에 위임
        menuService.updateMenus(id, request.getMenus());

        // 5. 정기 휴무일 업데이트는 RegularHolidayService에 위임
        regularHolidayService.updateRegularHolidaysForRestaurant(id, request.getRegularHolidayNumbers());

        // 6. 태그 정보 업데이트는 RestaurantTagService에 위임
        restaurantTagService.updateTagMappingsForRestaurant(id, request.getSelectedTagIds());

        // 변경된 내용을 DB에 즉시 반영하기 위해 save 호출
        restaurantRepository.save(restaurant);

        // 7. 업데이트된 전체 정보 다시 조회하여 반환
        return getRestaurantDetail(id, null);
    }
}
