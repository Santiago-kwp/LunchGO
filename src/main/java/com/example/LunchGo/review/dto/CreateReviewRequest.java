package com.example.LunchGo.review.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateReviewRequest {
    private Long userId;
    private Long reservationId;
    private Long receiptId;
    private Integer rating;
    private String content;
    private List<Long> tagIds;
    private List<String> imageUrls;
}
