package com.marchive.marchive_backend.chat.controller;

import com.marchive.marchive_backend.chat.dto.ChatDtos.ChatListResponse;
import com.marchive.marchive_backend.chat.dto.ChatDtos.MessageListResponse;
import com.marchive.marchive_backend.chat.dto.ChatDtos.SearchNewChatResponse;
import com.marchive.marchive_backend.chat.dto.ChatDtos.SearchRequest;
import com.marchive.marchive_backend.chat.dto.ChatDtos.SearchResponse;
import com.marchive.marchive_backend.chat.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    // 1. 채팅 목록 조회
    @GetMapping("/chat")
    public ResponseEntity<ChatListResponse> getChatList(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "0") Long cursor
    ) {
        return ResponseEntity.ok(chatService.getChatList(userId, cursor));
    }

    // 2. 메시지 목록 조회
    @GetMapping("/chat/{chatId}")
    public ResponseEntity<MessageListResponse> getMessageList(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long chatId,
            @RequestParam(defaultValue = "0") Long cursor
    ) {
        return ResponseEntity.ok(chatService.getMessageList(userId, chatId, cursor));
    }

    // 3. 기존 채팅방에서 검색
    @PostMapping("/search/{chatId}")
    public ResponseEntity<SearchResponse> search(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long chatId,
            @RequestBody SearchRequest request
    ) {
        return ResponseEntity.ok(chatService.search(userId, chatId, request.searchText()));
    }

    // 4. 새 채팅방 생성 + 검색
    @PostMapping("/search")
    public ResponseEntity<SearchNewChatResponse> searchNewChat(
            @AuthenticationPrincipal Long userId,
            @RequestBody SearchRequest request
    ) {
        return ResponseEntity.ok(chatService.searchNewChat(userId, request.searchText(), request.igAccountId()));
    }
}
