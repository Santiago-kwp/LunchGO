package com.example.LunchGo.bookmark.controller;

import com.example.LunchGo.bookmark.dto.BookmarkInfo;
import com.example.LunchGo.bookmark.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class BookmarkController {
    private final BookmarkService bookmarkService;

    @PostMapping("/bookmark")
    public ResponseEntity<?> addBookmark(@RequestBody BookmarkInfo bookmarkInfo) {
        if(bookmarkInfo.getUserId() == null || bookmarkInfo.getRestaurantId() == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        bookmarkService.save(bookmarkInfo); //이미 등록된 식당이면 409
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/bookmark")
    public ResponseEntity<?> deleteBookmark(@RequestBody BookmarkInfo bookmarkInfo) {
        if(bookmarkInfo.getUserId() == null || bookmarkInfo.getRestaurantId() == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        bookmarkService.delete(bookmarkInfo); //등록되어 있지 않은 식당이면 404
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
