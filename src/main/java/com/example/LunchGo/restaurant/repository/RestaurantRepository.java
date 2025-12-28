package com.example.LunchGo.restaurant.repository;

import com.example.LunchGo.restaurant.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    @Query("SELECT r FROM Restaurant r LEFT JOIN FETCH r.restaurantImages WHERE r.restaurantId = :restaurantId")
    Optional<Restaurant> findByIdWithImages(@Param("restaurantId") Long restaurantId);

    @Query(value = "SELECT tag_id FROM restaurant_tag_maps WHERE restaurant_id = :restaurantId", nativeQuery = true)
    List<Long> findTagIdsByRestaurantId(@Param("restaurantId") Long restaurantId);

    @Modifying
    @Query(value = "INSERT INTO restaurant_tag_maps (restaurant_id, tag_id) VALUES (:restaurantId, :tagId)", nativeQuery = true)
    void saveRestaurantTagMapping(@Param("restaurantId") Long restaurantId, @Param("tagId") Long tagId);

    @Query(
            value = "SELECT " +
                    "r.restaurant_id AS restaurantId, " +
                    "r.name AS name, " +
                    "r.road_address AS roadAddress, " +
                    "r.detail_address AS detailAddress, " +
                    "COALESCE(s.view_recent, 0) AS viewCount, " +
                    "COALESCE(s.confirm_recent, 0) AS confirmCount, " +
                    "COALESCE(rv.reviewCount, 0) AS reviewCount, " +
                    "COALESCE(rv.rating, 0) AS rating, " +
                    "(" +
                    "(COALESCE(s.confirm_recent, 0) * :confirmWeight) + " +
                    "(COALESCE(s.view_recent, 0) * :viewWeight) + " +
                    "CASE " +
                    "WHEN r.created_at >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) " +
                    "THEN :newbieWeight " +
                    "WHEN r.created_at >= DATE_SUB(CURDATE(), INTERVAL 60 DAY) " +
                    "THEN :newbieWeight * (60 - DATEDIFF(CURDATE(), r.created_at)) / 30 " +
                    "ELSE 0 " +
                    "END" +
                    ") AS score, " +
                    "img.imageUrl AS imageUrl, " +
                    "rt.tagIds AS tagIds, " +
                    "rt.tagContents AS tagContents, " +
                    "rvt.reviewTagIds AS reviewTagIds, " +
                    "rvt.reviewTagContents AS reviewTagContents, " +
                    "rvt.reviewTagCounts AS reviewTagCounts " +
                    "FROM restaurants r " +
                    "LEFT JOIN (" +
                    "SELECT restaurant_id, " +
                    "CAST(SUM(IFNULL(view_count, 0)) AS SIGNED) AS view_recent, " +
                    "CAST(SUM(IFNULL(confirm_count, 0)) AS SIGNED) AS confirm_recent " +
                    "FROM daily_restaurant_stats " +
                    "WHERE stat_date >= DATE_SUB(CURDATE(), INTERVAL :days DAY) " +
                    "GROUP BY restaurant_id" +
                    ") s ON r.restaurant_id = s.restaurant_id " +
                    "LEFT JOIN (" +
                    "SELECT restaurant_id, " +
                    "COUNT(*) AS reviewCount, " +
                    "ROUND(AVG(rating), 1) AS rating " +
                    "FROM reviews " +
                    "WHERE status = 'PUBLIC' " +
                    "GROUP BY restaurant_id" +
                    ") rv ON r.restaurant_id = rv.restaurant_id " +
                    "LEFT JOIN (" +
                    "SELECT restaurant_id, " +
                    "SUBSTRING_INDEX(GROUP_CONCAT(image_url ORDER BY restaurant_image_id), ',', 1) AS imageUrl " +
                    "FROM restaurant_images " +
                    "GROUP BY restaurant_id" +
                    ") img ON r.restaurant_id = img.restaurant_id " +
                    "LEFT JOIN (" +
                    "SELECT rtm.restaurant_id, " +
                    "GROUP_CONCAT(st.tag_id ORDER BY st.tag_id SEPARATOR ',') AS tagIds, " +
                    "GROUP_CONCAT(st.content ORDER BY st.tag_id SEPARATOR ',') AS tagContents " +
                    "FROM restaurant_tag_maps rtm " +
                    "JOIN search_tags st ON st.tag_id = rtm.tag_id " +
                    "GROUP BY rtm.restaurant_id" +
                    ") rt ON r.restaurant_id = rt.restaurant_id " +
                    "LEFT JOIN (" +
                    "SELECT restaurant_id, " +
                    "GROUP_CONCAT(tag_id ORDER BY tag_count DESC, tag_id SEPARATOR ',') AS reviewTagIds, " +
                    "GROUP_CONCAT(name ORDER BY tag_count DESC, tag_id SEPARATOR ',') AS reviewTagContents, " +
                    "GROUP_CONCAT(tag_count ORDER BY tag_count DESC, tag_id SEPARATOR ',') AS reviewTagCounts " +
                    "FROM (" +
                    "SELECT " +
                    "rv.restaurant_id, " +
                    "rt.tag_id, " +
                    "rt.name, " +
                    "COUNT(*) AS tag_count, " +
                    "ROW_NUMBER() OVER ( " +
                    "PARTITION BY rv.restaurant_id " +
                    "ORDER BY COUNT(*) DESC, rt.tag_id " +
                    ") AS rn " +
                    "FROM reviews rv " +
                    "JOIN review_tag_maps rtm ON rv.review_id = rtm.review_id " +
                    "JOIN review_tags rt ON rt.tag_id = rtm.tag_id " +
                    "WHERE rv.status = 'PUBLIC' AND rt.tag_type = 'USER' " +
                    "GROUP BY rv.restaurant_id, rt.tag_id, rt.name" +
                    ") ranked " +
                    "WHERE rn <= 3 " +
                    "GROUP BY restaurant_id" +
                    ") rvt ON r.restaurant_id = rvt.restaurant_id " +
                    "WHERE r.status = 'OPEN' " +
                    "ORDER BY score DESC " +
                    "LIMIT :limit",
            nativeQuery = true
    )
    List<TrendingRestaurantProjection> findTrendingRestaurants(
            @Param("days") int days,
            @Param("limit") int limit,
            @Param("newbieWeight") double newbieWeight,
            @Param("confirmWeight") double confirmWeight,
            @Param("viewWeight") double viewWeight
    );
    @Modifying
    @Query(value = "DELETE FROM restaurant_tag_maps WHERE restaurant_id = :restaurantId", nativeQuery = true)
    void deleteRestaurantTagMappingsByRestaurantId(@Param("restaurantId") Long restaurantId);
}
