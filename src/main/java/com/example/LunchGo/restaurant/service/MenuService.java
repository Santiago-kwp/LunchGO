package com.example.LunchGo.restaurant.service;

import com.example.LunchGo.restaurant.domain.MenuCategory;
import com.example.LunchGo.restaurant.dto.MenuDTO;
import com.example.LunchGo.restaurant.dto.MenuTagDTO;
import com.example.LunchGo.restaurant.dto.MenuTagMappingDTO;
import com.example.LunchGo.restaurant.entity.Menu;
import com.example.LunchGo.restaurant.repository.MenuRepository;
import com.example.LunchGo.tag.entity.SearchTag;
import com.example.LunchGo.tag.repository.SearchTagRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;
    private final SearchTagRepository searchTagRepository;
    private final ModelMapper modelMapper;

    /**
     * 특정 식당의 메뉴 리스트를 생성합니다.
     *
     * @param restaurantId 메뉴를 추가할 식당 ID
     * @param menuDtos     생성할 메뉴 정보 DTO 리스트
     */
    public void createMenus(Long restaurantId, List<MenuDTO> menuDtos) {
        if (menuDtos == null || menuDtos.isEmpty()) {
            return;
        }
        for (MenuDTO menuDto : menuDtos) {
            Menu menu = Menu.builder()
                    .restaurantId(restaurantId)
                    .name(menuDto.getName())
                    .price(menuDto.getPrice())
                    .category(menuDto.getCategory()) // .findCategory() 호출 제거
                    .description(menuDto.getDescription() != null ? menuDto.getDescription() : "")
                    .build();
            Menu savedMenu = menuRepository.save(menu);

            if (menuDto.getTags() != null && !menuDto.getTags().isEmpty()) {
                for (MenuTagDTO menuTagDto : menuDto.getTags()) {
                    if (menuTagDto.getTagId() != null) {
                        menuRepository.saveMenuTagMapping(savedMenu.getMenuId(), menuTagDto.getTagId());
                    }
                }
            }
        }
    }

    /**
     * 특정 식당의 단일 메뉴를 생성합니다.
     *
     * @param restaurantId 메뉴를 추가할 식당 ID
     * @param menuDto      생성할 메뉴 정보 DTO
     * @return 생성된 메뉴 DTO
     */
    public MenuDTO createMenu(Long restaurantId, MenuDTO menuDto) {
        Menu menu = Menu.builder()
                .restaurantId(restaurantId)
                .name(menuDto.getName())
                .price(menuDto.getPrice())
                .category(menuDto.getCategory())
                .description(menuDto.getDescription() != null ? menuDto.getDescription() : "")
                .build();
        Menu savedMenu = menuRepository.save(menu);
        updateMenuTags(savedMenu.getMenuId(), menuDto.getTags());

        MenuDTO response = modelMapper.map(savedMenu, MenuDTO.class);
        response.setTags(menuDto.getTags() != null ? menuDto.getTags() : Collections.emptyList());
        response.setImageUrl(menuDto.getImageUrl());
        return response;
    }

    /**
     * 특정 식당의 단일 메뉴를 업데이트합니다.
     *
     * @param restaurantId 식당 ID
     * @param menuId       메뉴 ID
     * @param menuDto      업데이트할 메뉴 정보 DTO
     * @return 업데이트된 메뉴 DTO
     */
    public MenuDTO updateMenu(Long restaurantId, Long menuId, MenuDTO menuDto) {
        Menu menu = menuRepository.findByMenuIdAndRestaurantIdAndIsDeletedFalse(menuId, restaurantId)
                .orElseThrow(() -> new NoSuchElementException("Menu not found with id: " + menuId));

        if (menuDto.getName() != null) {
            menu.setName(menuDto.getName());
        }
        if (menuDto.getCategory() != null) {
            menu.setCategory(menuDto.getCategory());
        }
        if (menuDto.getDescription() != null) {
            menu.setDescription(menuDto.getDescription());
        }
        if (menuDto.getPrice() != null) {
            menu.setPrice(menuDto.getPrice());
        }

        Menu savedMenu = menuRepository.save(menu);
        updateMenuTags(savedMenu.getMenuId(), menuDto.getTags());

        MenuDTO response = modelMapper.map(savedMenu, MenuDTO.class);
        response.setTags(menuDto.getTags() != null ? menuDto.getTags() : Collections.emptyList());
        response.setImageUrl(menuDto.getImageUrl());
        return response;
    }

    /**
     * 특정 식당의 단일 메뉴를 삭제합니다. (소프트 삭제)
     *
     * @param restaurantId 식당 ID
     * @param menuId       메뉴 ID
     */
    public void deleteMenu(Long restaurantId, Long menuId) {
        int updated = menuRepository.softDeleteMenu(restaurantId, menuId);
        if (updated == 0) {
            throw new NoSuchElementException("Menu not found with id: " + menuId);
        }
        menuRepository.deleteMenuTagMappingsByMenuId(menuId);
    }

    /**
     * 특정 식당의 메뉴 리스트를 업데이트합니다. (변경분 비교 방식 + ModelMapper)
     *
     * @param restaurantId    업데이트할 식당 ID
     * @param updatedMenuDtos 업데이트할 메뉴 정보 DTO 리스트
     */
    public void updateMenus(Long restaurantId, List<MenuDTO> updatedMenuDtos) {
        if (updatedMenuDtos == null) {
            updatedMenuDtos = new ArrayList<>(); // null이면 빈 리스트로 처리하여 모든 메뉴를 삭제
        }

        // 1. DB에서 기존 메뉴 목록 조회
        Map<Long, Menu> existingMenuMap = menuRepository.findAllByRestaurantIdAndIsDeletedFalse(restaurantId)
                .stream()
                .collect(Collectors.toMap(Menu::getMenuId, menu -> menu));

        // 2. 요청 DTO에서 메뉴 ID 집합 생성
        Set<Long> updatedMenuIds = updatedMenuDtos.stream()
                .map(MenuDTO::getMenuId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 3. 신규 및 수정 메뉴 처리
        for (MenuDTO dto : updatedMenuDtos) {
            Long menuId = dto.getMenuId();

            // 휴리스틱: 1,000,000보다 큰 ID는 클라이언트에서 생성된 임시 ID로 간주
            // 실제 DB ID는 보통 이보다 훨씬 작음
            boolean isNew = (menuId == null || menuId > 1000000L);

            if (isNew) {
                // 신규 메뉴 -> INSERT
                Menu newMenu = modelMapper.map(dto, Menu.class);
                newMenu.setRestaurantId(restaurantId);
                newMenu.setMenuId(null); // DB에 저장하기 전, ID를 null로 설정하여 auto-increment 적용

                Menu savedMenu = menuRepository.save(newMenu);
                updateMenuTags(savedMenu.getMenuId(), dto.getTags());
            } else {
                // 기존 메뉴 -> UPDATE
                Menu existingMenu = existingMenuMap.get(menuId);
                if (existingMenu != null) {
                    modelMapper.map(dto, existingMenu);
                    menuRepository.save(existingMenu); // 변경된 메뉴를 명시적으로 저장
                    updateMenuTags(existingMenu.getMenuId(), dto.getTags());
                }
            }
        }

        // 4. 삭제된 메뉴 처리 (차집합 활용)
        Set<Long> existingMenuIds = new HashSet<>(existingMenuMap.keySet());
        existingMenuIds.removeAll(updatedMenuIds); // existingMenuIds에 삭제할 ID만 남게 됨

        for (Long menuIdToDelete : existingMenuIds) {
            menuRepository.softDeleteMenu(restaurantId, menuIdToDelete);
            menuRepository.deleteMenuTagMappingsByMenuId(menuIdToDelete);
        }
    }

    /**
     * 특정 메뉴의 태그를 업데이트합니다. (기존 태그 모두 삭제 후 새로 추가)
     *
     * @param menuId  태그를 업데이트할 메뉴 ID
     * @param tagDtos 새로운 태그 DTO 리스트
     */
    private void updateMenuTags(Long menuId, List<MenuTagDTO> tagDtos) {
        menuRepository.deleteMenuTagMappingsByMenuId(menuId);
        if (tagDtos != null && !tagDtos.isEmpty()) {
            for (MenuTagDTO tagDto : tagDtos) {
                if (tagDto.getTagId() != null) {
                    menuRepository.saveMenuTagMapping(menuId, tagDto.getTagId());
                }
            }
        }
    }


    /**
     * 특정 식당의 메뉴 리스트를 조회합니다. (N+1 문제 해결 버전)
     *
     * @param restaurantId 조회할 식당 ID
     * @return 메뉴 DTO 리스트
     */
    @Transactional(readOnly = true)
    public List<MenuDTO> getMenusByRestaurant(Long restaurantId) {
        List<Menu> menus = menuRepository.findAllByRestaurantIdAndIsDeletedFalse(restaurantId);
        if (menus.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> menuIds = menus.stream().map(Menu::getMenuId).collect(Collectors.toList());
        List<MenuTagMappingDTO> mappings = menuRepository.findTagsForMenus(menuIds);

        List<Long> tagIds = mappings.stream().map(MenuTagMappingDTO::getTagId).distinct().collect(Collectors.toList());
        Map<Long, SearchTag> tagMap = searchTagRepository.findAllById(tagIds).stream()
                .collect(Collectors.toMap(SearchTag::getTagId, tag -> tag));

        Map<Long, List<Long>> menuIdToTagIdsMap = mappings.stream()
                .collect(Collectors.groupingBy(MenuTagMappingDTO::getMenuId,
                        Collectors.mapping(MenuTagMappingDTO::getTagId, Collectors.toList())));

        return menus.stream().map(menu -> {
            MenuDTO menuDto = modelMapper.map(menu, MenuDTO.class);
            List<Long> relatedTagIds = menuIdToTagIdsMap.getOrDefault(menu.getMenuId(), Collections.emptyList());
            List<MenuTagDTO> menuTagDtos = relatedTagIds.stream()
                    .map(tagId -> {
                        SearchTag tag = tagMap.get(tagId);
                        return tag != null ? modelMapper.map(tag, MenuTagDTO.class) : null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            menuDto.setTags(menuTagDtos);
            return menuDto;
        }).collect(Collectors.toList());
    }

    /**
     * 특정 식당의 단일 메뉴 정보를 조회합니다.
     *
     * @param restaurantId 식당 ID
     * @param menuId       메뉴 ID
     * @return 메뉴 DTO
     */
    @Transactional(readOnly = true)
    public MenuDTO getMenuByRestaurant(Long restaurantId, Long menuId) {
        Menu menu = menuRepository.findByMenuIdAndRestaurantIdAndIsDeletedFalse(menuId, restaurantId)
                .orElseThrow(() -> new NoSuchElementException("Menu not found with id: " + menuId));

        List<MenuTagMappingDTO> mappings = menuRepository.findTagsForMenus(List.of(menuId));
        List<Long> tagIds = mappings.stream().map(MenuTagMappingDTO::getTagId).distinct().collect(Collectors.toList());
        Map<Long, SearchTag> tagMap = tagIds.isEmpty()
                ? Collections.emptyMap()
                : searchTagRepository.findAllById(tagIds).stream()
                        .collect(Collectors.toMap(SearchTag::getTagId, tag -> tag));

        List<MenuTagDTO> menuTagDtos = mappings.stream()
                .map(mapping -> tagMap.get(mapping.getTagId()))
                .filter(Objects::nonNull)
                .map(tag -> modelMapper.map(tag, MenuTagDTO.class))
                .collect(Collectors.toList());

        MenuDTO menuDto = modelMapper.map(menu, MenuDTO.class);
        menuDto.setTags(menuTagDtos);
        return menuDto;
    }

    /**
     * 특정 식당의 메뉴 이미지 목록을 조회합니다.
     *
     * @param restaurantId 식당 ID
     * @param menuId       메뉴 ID
     * @return 이미지 URL 리스트
     */
    @Transactional(readOnly = true)
    public List<String> getMenuImages(Long restaurantId, Long menuId) {
        menuRepository.findByMenuIdAndRestaurantIdAndIsDeletedFalse(menuId, restaurantId)
                .orElseThrow(() -> new NoSuchElementException("Menu not found with id: " + menuId));

        return menuRepository.findMenuImageUrls(menuId);
    }
}
