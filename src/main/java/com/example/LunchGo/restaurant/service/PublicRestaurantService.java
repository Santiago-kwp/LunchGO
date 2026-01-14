package com.example.LunchGo.restaurant.service;

import com.example.LunchGo.restaurant.dto.ImageDTO;
import com.example.LunchGo.restaurant.dto.MenuDTO;
import com.example.LunchGo.restaurant.dto.RegularHolidayDTO;
import com.example.LunchGo.restaurant.dto.RestaurantDetailResponse;
import com.example.LunchGo.restaurant.dto.RestaurantTagDTO;
import com.example.LunchGo.restaurant.entity.Restaurant;
import com.example.LunchGo.restaurant.entity.RestaurantSummary;
import com.example.LunchGo.restaurant.repository.RestaurantRepository;
import com.example.LunchGo.restaurant.stats.RestaurantStatsEventService;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import com.example.LunchGo.restaurant.dto.RestaurantSummaryResponse;
import com.example.LunchGo.restaurant.repository.RestaurantSummaryRepository;
import com.example.LunchGo.map.service.KakaoGeoService;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PublicRestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantSummaryRepository restaurantSummaryRepository;
    private final MenuService menuService;
    private final RegularHolidayService regularHolidayService;
    private final RestaurantTagService restaurantTagService;
    private final RestaurantStatsEventService statsEventService;
    private final KakaoGeoService kakaoGeoService;
    private final ModelMapper modelMapper;

    /**
     * 식당 전체 목록을 조회합니다. (캐싱 적용)
     * - Cache Hit: Redis에서 즉시 반환
     * - Cache Miss: DB 조회 + 좌표 변환 수행 후 Redis 저장
     */
    @Cacheable(value = "restaurantSummaries", key = "'all'", unless = "#result == null")
    public List<RestaurantSummaryResponse> getRestaurantSummaries() {
        return fetchSummariesFromDb();
    }

    /**
     * 식당 전체 목록 캐시를 강제로 갱신합니다. (캐시 웜업용)
     * - @CachePut: 메서드 실행 결과를 무조건 캐시에 덮어씌웁니다.
     * - 스케줄러에 의해 주기적으로 호출되어 캐시 만료를 방지하고 최신 데이터를 유지합니다.
     */
    @CachePut(value = "restaurantSummaries", key = "'all'", unless = "#result == null")
    public List<RestaurantSummaryResponse> refreshRestaurantSummaries() {
        log.debug("Refreshing restaurant summaries cache...");
        return fetchSummariesFromDb();
    }

    // 실제 비즈니스 로직 (DB 조회 + 좌표 변환) 추출
    private List<RestaurantSummaryResponse> fetchSummariesFromDb() {
        return restaurantSummaryRepository.findAll().parallelStream()
                .map(entity -> {
                    // 1. 엔티티를 DTO로 변환
                    RestaurantSummaryResponse response = mapToResponse(entity);
                    
                    // 2. 순수 도로명 주소로 좌표 조회
                    KakaoGeoService.GeoCoordinate coords = kakaoGeoService.getCoordinateByAddress(entity.getRoadAddress());
                    
                    if (coords != null) {
                        response.setLatitude(coords.latitude());
                        response.setLongitude(coords.longitude());
                        return response;
                    }
                    return null; // 좌표 없으면 제외 대상
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private RestaurantSummaryResponse mapToResponse(RestaurantSummary entity) {
        String fullAddress = entity.getRoadAddress();
        if (entity.getDetailAddress() != null && !entity.getDetailAddress().isBlank()) {
            fullAddress += " " + entity.getDetailAddress();
        }

        return RestaurantSummaryResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .image(entity.getImage())
                .roadAddress(fullAddress) // 화면 표시용 (전체 주소)
                .detailAddress(entity.getDetailAddress())
                .rating(entity.getRating())
                .reviews(entity.getReviews())
                .category(entity.getCategory())
                .price(entity.getPrice())
                .build();
    }

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
