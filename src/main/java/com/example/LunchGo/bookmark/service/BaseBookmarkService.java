package com.example.LunchGo.bookmark.service;

import com.example.LunchGo.bookmark.dto.BookmarkInfo;
import com.example.LunchGo.bookmark.entity.Bookmark;
import com.example.LunchGo.bookmark.repository.BookmarkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Log4j2
public class BaseBookmarkService implements BookmarkService {
    private final BookmarkRepository bookmarkRepository;

    @Override
    public void save(BookmarkInfo bookmarkInfo) {
        //존재하는 즐겨찾기인지 확인
        if(bookmarkRepository.existsByUserIdAndRestaurantId(bookmarkInfo.getUserId(), bookmarkInfo.getRestaurantId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 즐겨찾기에 등록되어 있습니다.");
        }

        bookmarkRepository.save(Bookmark.builder()
                .userId(bookmarkInfo.getUserId()) //해당 사용자
                .restaurantId(bookmarkInfo.getRestaurantId()) //식당Id를 즐겨찾기에 등록
                        .promotionAgree(false) //기본값 false
                .build());
    }

    @Override
    @Transactional
    public void delete(BookmarkInfo bookmarkInfo) {
        if(!bookmarkRepository.existsByUserIdAndRestaurantId(bookmarkInfo.getUserId(), bookmarkInfo.getRestaurantId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "즐겨찾기에 등록되어있지 않습니다.");
        }

        bookmarkRepository.deleteByRestaurantIdAndUserId(bookmarkInfo.getRestaurantId(), bookmarkInfo.getUserId());
    }
}
