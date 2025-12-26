package com.example.LunchGo.bookmark.service;

import com.example.LunchGo.bookmark.dto.BookmarkInfo;

public interface BookmarkService {
    void save(BookmarkInfo bookmarkInfo);

    void delete(BookmarkInfo bookmarkInfo);
}
