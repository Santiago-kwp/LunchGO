package com.example.LunchGo.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantUpdateRequest {
    private String name;
    private String phone;
    private String roadAddress;
    private String detailAddress;
    private String description;
    private Integer reservationLimit;
    private boolean holidayAvailable;
    private boolean preorderAvailable;
    private String openTime; // "HH:mm"
    private String closeTime; // "HH:mm"
    private LocalDate openDate;
    private List<Integer> regularHolidayNumbers;
    private List<Long> selectedTagIds;
}
