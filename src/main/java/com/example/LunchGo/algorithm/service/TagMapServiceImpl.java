package com.example.LunchGo.algorithm.service;

import com.example.LunchGo.algorithm.dto.TagMapDTO;
import com.example.LunchGo.restaurant.repository.RestaurantRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TagMapServiceImpl implements TagMapService {

    private final RestaurantRepository restaurantRepository;

    @Override
    public List<TagMapDTO> getTagSimilarityByUser(Long userId) {
        return restaurantRepository.findRestaurantsByUserTagSimilarity(userId).stream()
            .map(TagMapDTO::fromProjection)
            .toList();
    }
}
