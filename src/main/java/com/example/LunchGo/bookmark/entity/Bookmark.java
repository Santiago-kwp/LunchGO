package com.example.LunchGo.bookmark.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "bookmarks")
public class Bookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bookmark_id")
    private Long bookmarkId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;

    @Column(name = "promotion_agree")
    private Boolean promotionAgree;

    @Builder
    public Bookmark(Long userId, Long restaurantId, Boolean promotionAgree) {
        this.userId = userId;
        this.restaurantId = restaurantId;
        this.promotionAgree = promotionAgree;
    }
}
