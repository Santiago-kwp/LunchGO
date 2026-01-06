package com.example.LunchGo.restaurant.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List; // List import 추가

@Data
public class RestaurantSearchParameter {
    private LocalDate date;
    private String time;
    private Integer partySize;
    // TODO: 향후 태그 검색 기능 추가 시, 여기에 List<String> tags; 필드 추가
}
