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
                    "COALESCE((" +
                    "SELECT COUNT(*) " +
                    "FROM reviews rv " +
                    "WHERE rv.restaurant_id = r.restaurant_id " +
                    "AND rv.status = 'PUBLIC'" +
                    "), 0) AS reviewCount, " +
                    "COALESCE((" +
                    "SELECT ROUND(AVG(rv.rating), 1) " +
                    "FROM reviews rv " +
                    "WHERE rv.restaurant_id = r.restaurant_id " +
                    "AND rv.status = 'PUBLIC'" +
                    "), 0) AS rating, " +
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
                    "(" +
                    "SELECT ri.image_url " +
                    "FROM restaurant_images ri " +
                    "WHERE ri.restaurant_id = r.restaurant_id " +
                    "ORDER BY ri.restaurant_image_id " +
                    "LIMIT 1" +
                    ") AS imageUrl, " +
                    "(" +
                    "SELECT GROUP_CONCAT(st.tag_id ORDER BY st.tag_id SEPARATOR ',') " +
                    "FROM restaurant_tag_maps rtm " +
                    "JOIN search_tags st ON st.tag_id = rtm.tag_id " +
                    "WHERE rtm.restaurant_id = r.restaurant_id" +
                    ") AS tagIds, " +
                    "(" +
                    "SELECT GROUP_CONCAT(st.content ORDER BY st.tag_id SEPARATOR ',') " +
                    "FROM restaurant_tag_maps rtm " +
                    "JOIN search_tags st ON st.tag_id = rtm.tag_id " +
                    "WHERE rtm.restaurant_id = r.restaurant_id" +
                    ") AS tagContents " +
                    ", " +
                    "(" +
                    "SELECT GROUP_CONCAT(t.tag_id ORDER BY t.tag_count DESC, t.tag_id SEPARATOR ',') " +
                    "FROM (" +
                    "SELECT rt.tag_id AS tag_id, COUNT(*) AS tag_count " +
                    "FROM reviews rv " +
                    "JOIN review_tag_maps rtm ON rv.review_id = rtm.review_id " +
                    "JOIN review_tags rt ON rt.tag_id = rtm.tag_id " +
                    "WHERE rv.restaurant_id = r.restaurant_id " +
                    "AND rv.status = 'PUBLIC' " +
                    "AND rt.tag_type = 'USER' " +
                    "GROUP BY rt.tag_id " +
                    "ORDER BY tag_count DESC, rt.tag_id " +
                    "LIMIT 3" +
                    ") t" +
                    ") AS reviewTagIds, " +
                    "(" +
                    "SELECT GROUP_CONCAT(t.name ORDER BY t.tag_count DESC, t.tag_id SEPARATOR ',') " +
                    "FROM (" +
                    "SELECT rt.tag_id AS tag_id, rt.name AS name, COUNT(*) AS tag_count " +
                    "FROM reviews rv " +
                    "JOIN review_tag_maps rtm ON rv.review_id = rtm.review_id " +
                    "JOIN review_tags rt ON rt.tag_id = rtm.tag_id " +
                    "WHERE rv.restaurant_id = r.restaurant_id " +
                    "AND rv.status = 'PUBLIC' " +
                    "AND rt.tag_type = 'USER' " +
                    "GROUP BY rt.tag_id, rt.name " +
                    "ORDER BY tag_count DESC, rt.tag_id " +
                    "LIMIT 3" +
                    ") t" +
                    ") AS reviewTagContents, " +
                    "(" +
                    "SELECT GROUP_CONCAT(t.tag_count ORDER BY t.tag_count DESC, t.tag_id SEPARATOR ',') " +
                    "FROM (" +
                    "SELECT rt.tag_id AS tag_id, COUNT(*) AS tag_count " +
                    "FROM reviews rv " +
                    "JOIN review_tag_maps rtm ON rv.review_id = rtm.review_id " +
                    "JOIN review_tags rt ON rt.tag_id = rtm.tag_id " +
                    "WHERE rv.restaurant_id = r.restaurant_id " +
                    "AND rv.status = 'PUBLIC' " +
                    "AND rt.tag_type = 'USER' " +
                    "GROUP BY rt.tag_id " +
                    "ORDER BY tag_count DESC, rt.tag_id " +
                    "LIMIT 3" +
                    ") t" +
                    ") AS reviewTagCounts " +
                    "FROM restaurants r " +
                    "LEFT JOIN (" +
                    "SELECT restaurant_id, " +
                    "CAST(SUM(IFNULL(view_count, 0)) AS SIGNED) AS view_recent, " +
                    "CAST(SUM(IFNULL(confirm_count, 0)) AS SIGNED) AS confirm_recent " +
                    "FROM daily_restaurant_stats " +
                    "WHERE stat_date >= DATE_SUB(CURDATE(), INTERVAL :days DAY) " +
                    "GROUP BY restaurant_id" +
                    ") s ON r.restaurant_id = s.restaurant_id " +
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
