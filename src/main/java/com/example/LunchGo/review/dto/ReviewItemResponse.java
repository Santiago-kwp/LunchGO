package com.example.LunchGo.review.dto;

import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewItemResponse {
    private Long reviewId;
    private String author;
    private String company;
    private Integer visitCount;
    private Integer rating;
    private String content;
    private List<TagResponse> tags;
    private List<String> images;
    private LocalDate createdAt;
    private String status;
    private boolean isBlinded;
    private String blindReason;
}
