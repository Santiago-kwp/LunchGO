package com.example.LunchGo.review.service;

import com.example.LunchGo.review.domain.ReviewSort;
import com.example.LunchGo.review.dto.CreateReviewRequest;
import com.example.LunchGo.review.dto.CreateReviewResponse;
import com.example.LunchGo.review.dto.PageInfo;
import com.example.LunchGo.review.dto.ReceiptItemResponse;
import com.example.LunchGo.review.dto.RestaurantReviewListResponse;
import com.example.LunchGo.review.dto.ReviewEditResponse;
import com.example.LunchGo.review.dto.ReviewDetailResponse;
import com.example.LunchGo.review.dto.ReviewItemResponse;
import com.example.LunchGo.review.dto.ReviewListQuery;
import com.example.LunchGo.review.dto.ReviewSummary;
import com.example.LunchGo.review.dto.TagResponse;
import com.example.LunchGo.review.dto.UpdateReviewRequest;
import com.example.LunchGo.review.dto.UpdateReviewResponse;
import com.example.LunchGo.review.dto.VisitInfo;
import com.example.LunchGo.review.entity.Review;
import com.example.LunchGo.review.entity.ReviewImage;
import com.example.LunchGo.review.entity.ReviewTagMap;
import com.example.LunchGo.review.mapper.ReviewReadMapper;
import com.example.LunchGo.review.repository.ReviewImageRepository;
import com.example.LunchGo.review.repository.ReviewRepository;
import com.example.LunchGo.review.repository.ReviewTagMapRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewTagMapRepository reviewTagMapRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final ReviewReadMapper reviewReadMapper;

    @Override
    @Transactional
    public CreateReviewResponse createReview(Long restaurantId, CreateReviewRequest request) {
        validateCreateRequest(restaurantId, request);

        Review review = new Review(
            restaurantId,
            request.getUserId(),
            request.getReceiptId(),
            request.getRating(),
            request.getContent()
        );
        Review saved = reviewRepository.save(review);

        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            List<ReviewTagMap> maps = new ArrayList<>();
            for (Long tagId : request.getTagIds()) {
                maps.add(new ReviewTagMap(saved.getReviewId(), tagId));
            }
            reviewTagMapRepository.saveAll(maps);
        }

        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            List<ReviewImage> images = new ArrayList<>();
            int sortOrder = 0;
            for (String imageUrl : request.getImageUrls()) {
                images.add(new ReviewImage(saved.getReviewId(), imageUrl, sortOrder++));
            }
            reviewImageRepository.saveAll(images);
        }

        return new CreateReviewResponse(saved.getReviewId(), saved.getCreatedAt(), saved.getStatus());
    }

    @Override
    public RestaurantReviewListResponse getRestaurantReviews(Long restaurantId, int page, int size, ReviewSort sort, List<Long> tagIds) {
        int resolvedPage = Math.max(page, 1);
        int resolvedSize = Math.min(Math.max(size, 1), 50);
        int offset = (resolvedPage - 1) * resolvedSize;

        ReviewListQuery query = new ReviewListQuery(restaurantId, resolvedSize, offset, sort, tagIds);

        ReviewSummary summary = reviewReadMapper.selectReviewSummary(restaurantId);
        if (summary == null) {
            summary = new ReviewSummary(0.0, 0L, Collections.emptyList());
        } else {
            summary.setTopTags(reviewReadMapper.selectTopTags(restaurantId));
        }

        List<ReviewItemResponse> items = reviewReadMapper.selectReviewItems(query);
        if (items == null) {
            items = Collections.emptyList();
        } else {
            for (ReviewItemResponse item : items) {
                List<TagResponse> tags = reviewReadMapper.selectReviewTags(item.getReviewId());
                List<String> images = reviewReadMapper.selectReviewImages(item.getReviewId());
                item.setTags(tags);
                item.setImages(images);
            }
        }

        long total = reviewReadMapper.selectReviewCount(query);
        PageInfo pageInfo = new PageInfo(resolvedPage, resolvedSize, total);

        return new RestaurantReviewListResponse(summary, items, pageInfo);
    }

    @Override
    public ReviewDetailResponse getReviewDetail(Long restaurantId, Long reviewId) {
        if (!reviewRepository.existsByReviewIdAndRestaurantId(reviewId, restaurantId)) {
            return null;
        }

        ReviewDetailResponse detail = reviewReadMapper.selectReviewDetail(reviewId);
        if (detail == null) {
            return null;
        }

        detail.setTags(reviewReadMapper.selectReviewTags(reviewId));
        detail.setImages(reviewReadMapper.selectReviewImages(reviewId));
        detail.setComments(reviewReadMapper.selectReviewComments(reviewId));

        VisitInfo visitInfo = reviewReadMapper.selectVisitInfo(reviewId);
        if (visitInfo != null) {
            List<ReceiptItemResponse> items = reviewReadMapper.selectReceiptItemsByReviewId(reviewId);
            visitInfo.setMenuItems(items);
        }
        detail.setVisitInfo(visitInfo);

        return detail;
    }

    @Override
    public ReviewEditResponse getReviewEdit(Long restaurantId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId).orElse(null);
        if (review == null || !restaurantId.equals(review.getRestaurantId())) {
            return null;
        }

        ReviewEditResponse response = new ReviewEditResponse();
        response.setReviewId(reviewId);
        response.setRestaurantId(review.getRestaurantId());
        response.setReceiptId(review.getReceiptId());
        response.setRating(review.getRating());
        response.setContent(review.getContent());
        response.setTags(reviewReadMapper.selectReviewTags(reviewId));
        response.setImages(reviewReadMapper.selectReviewImages(reviewId));

        VisitInfo visitInfo = reviewReadMapper.selectVisitInfo(reviewId);
        if (visitInfo != null) {
            List<ReceiptItemResponse> items = reviewReadMapper.selectReceiptItemsByReviewId(reviewId);
            visitInfo.setMenuItems(items);
        }
        response.setVisitInfo(visitInfo);

        return response;
    }

    @Override
    @Transactional
    public UpdateReviewResponse updateReview(Long restaurantId, Long reviewId, UpdateReviewRequest request) {
        validateUpdateRequest(restaurantId, request);

        Review review = reviewRepository.findById(reviewId).orElse(null);
        if (review == null || !restaurantId.equals(review.getRestaurantId())) {
            return null;
        }

        review.updateContent(request.getContent(), request.getRating());
        if (request.getReceiptId() != null) {
            review.updateReceiptId(request.getReceiptId());
        }

        reviewTagMapRepository.deleteByIdReviewId(reviewId);
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            List<ReviewTagMap> maps = new ArrayList<>();
            for (Long tagId : request.getTagIds()) {
                maps.add(new ReviewTagMap(reviewId, tagId));
            }
            reviewTagMapRepository.saveAll(maps);
        }

        reviewImageRepository.deleteByReviewId(reviewId);
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            List<ReviewImage> images = new ArrayList<>();
            int sortOrder = 0;
            for (String imageUrl : request.getImageUrls()) {
                images.add(new ReviewImage(reviewId, imageUrl, sortOrder++));
            }
            reviewImageRepository.saveAll(images);
        }

        Review saved = reviewRepository.save(review);
        return new UpdateReviewResponse(saved.getReviewId(), saved.getUpdatedAt(), saved.getStatus());
    }

    @Override
    @Transactional
    public boolean deleteReview(Long restaurantId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId).orElse(null);
        if (review == null || !restaurantId.equals(review.getRestaurantId())) {
            return false;
        }
        reviewRepository.delete(review);
        return true;
    }

    private void validateCreateRequest(Long restaurantId, CreateReviewRequest request) {
        if (restaurantId == null) {
            throw new IllegalArgumentException("restaurantId is required");
        }
        if (request == null) {
            throw new IllegalArgumentException("request is required");
        }
        if (request.getUserId() == null) {
            throw new IllegalArgumentException("userId is required");
        }
        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            throw new IllegalArgumentException("rating must be between 1 and 5");
        }
    }

    private void validateUpdateRequest(Long restaurantId, UpdateReviewRequest request) {
        if (restaurantId == null) {
            throw new IllegalArgumentException("restaurantId is required");
        }
        if (request == null) {
            throw new IllegalArgumentException("request is required");
        }
        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            throw new IllegalArgumentException("rating must be between 1 and 5");
        }
    }
}
