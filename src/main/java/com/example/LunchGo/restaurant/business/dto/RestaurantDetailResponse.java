package com.example.LunchGo.restaurant.business.dto;

import com.example.LunchGo.restaurant.domain.RestaurantStatus;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantDetailResponse {

    private Long restaurantId;
    private String name;
    private String phone;
    private String roadAddress;
    private String detailAddress;
    private String description;
    private RestaurantStatus status;

    private Integer avgMainPrice;
    private Integer reservationLimit;

    private boolean holidayAvailable;
    private boolean preorderAvailable;

    private String openTime; // "HH:mm" format
    private String closeTime; // "HH:mm" format
    private LocalDate openDate;

    private List<ImageDto> images;
    private List<RestaurantTagDto> tags;
    private List<RegularHolidayDto> regularHolidays;

}
