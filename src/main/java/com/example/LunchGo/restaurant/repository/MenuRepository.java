package com.example.LunchGo.restaurant.repository;

import com.example.LunchGo.restaurant.domain.MenuCategory;
import com.example.LunchGo.restaurant.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import com.example.LunchGo.restaurant.dto.MenuTagMappingDTO; // MenuTagMappingDto import 예정

public interface MenuRepository extends JpaRepository<Menu, Long> {

    List<Menu> findAllByRestaurantIdAndIsDeletedFalse(Long restaurantId);

    Optional<Menu> findByMenuIdAndRestaurantIdAndIsDeletedFalse(Long menuId, Long restaurantId);

    // 참고: `updateMenu` 메서드는 제거되었습니다. 이제 메뉴 업데이트는 `MenuService.updateMenusForRestaurant`에서
    // JPA의 변경 감지(dirty checking) 메커니즘을 통해 처리됩니다. `isDeleted = false` 조건 검사는
    // `findAllByRestaurantIdAndIsDeletedFalse`를 통한 초기 조회 시 암묵적으로 처리됩니다.

    @Modifying
    @Query("""
            UPDATE Menu m
               SET m.isDeleted = true
             WHERE m.menuId = :menuId
               AND m.restaurantId = :restaurantId
               AND m.isDeleted = false
            """)
    int softDeleteMenu(@Param("restaurantId") Long restaurantId,
                       @Param("menuId") Long menuId);

    // N+1 문제 해결을 위한 조회 메서드 (DTO 클래스 반환)
    @Query(value = "SELECT menu_id as menuId, tag_id as tagId FROM menu_tag_maps WHERE menu_id IN (:menuIds)", nativeQuery = true)
    List<MenuTagMappingDTO> findTagsForMenus(@Param("menuIds") List<Long> menuIds);

    @Modifying
    @Query(value = "INSERT INTO menu_tag_maps (menu_id, tag_id) VALUES (:menuId, :tagId)", nativeQuery = true)
    void saveMenuTagMapping(@Param("menuId") Long menuId, @Param("tagId") Long tagId);

    @Modifying
    @Query(value = "DELETE FROM menu_tag_maps WHERE menu_id = :menuId", nativeQuery = true)
    void deleteMenuTagMappingsByMenuId(@Param("menuId") Long menuId);

    @Query(value = "SELECT image_url FROM menu_images WHERE menu_id = :menuId", nativeQuery = true)
    List<String> findMenuImageUrls(@Param("menuId") Long menuId);
}
