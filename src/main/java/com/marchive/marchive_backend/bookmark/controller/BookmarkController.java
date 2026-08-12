package com.marchive.marchive_backend.bookmark.controller;

import com.marchive.marchive_backend.bookmark.dto.BookmarkDtos.BulkRequest;
import com.marchive.marchive_backend.bookmark.dto.BookmarkDtos.BulkResponse;
import com.marchive.marchive_backend.bookmark.service.BookmarkService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BookmarkController {

    private final BookmarkService bookmarkService;

    public BookmarkController(BookmarkService bookmarkService) {
        this.bookmarkService = bookmarkService;
    }

    @PostMapping("/bulk")
    public ResponseEntity<BulkResponse> saveBulk(
            @AuthenticationPrincipal Long userId,
            @RequestBody BulkRequest request
    ) {
        return ResponseEntity.ok(bookmarkService.saveBulk(userId, request));
    }
}
