package com.example.LunchGo.restaurant.business.dto;

import com.example.LunchGo.tag.domain.TagCategory;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantTagDto {
    private Long tagId;
    private String content;
    private TagCategory category;
}
