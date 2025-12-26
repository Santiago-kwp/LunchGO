package com.example.LunchGo.bookmark.repository;

import com.example.LunchGo.bookmark.entity.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    /**
     * 사용자 즐겨찾기 삭제
     * */
    @Modifying
    @Query("DELETE FROM Bookmark b WHERE b.bookmarkId = :bookmarkId AND b.userId = :userId")
    int deleteByBookmarkId(Long bookmarkId, Long userId);

    /**
     * 즐겨찾기 여부 확인
     * */
    boolean existsByBookmarkId(Long bookmarkId);

    boolean existsByUserIdAndRestaurantId(Long userId, Long restaurantId);
}
