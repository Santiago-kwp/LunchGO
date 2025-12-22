package com.example.LunchGo.review.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateReviewRequest {
    private Long receiptId;
    private Integer rating;
    private String content;
    private List<Long> tagIds;
    private List<String> imageUrls;
}
