package com.marchive.marchive_backend.chat.dto;

import java.util.List;

public class ChatDtos {

    public record BookmarkDto(Long id, String thumbnailUrl, String contentUrl) {
    }

    public record MessageDto(Long id, String role, String contents, List<BookmarkDto> bookmarkList) {
    }

    public record ChatDto(Long id, String title) {
    }

    public record ChatWithMessagesDto(Long id, String title, List<MessageDto> initialMessageList) {
    }

    public record ChatListResponse(List<ChatDto> chatList) {
    }

    public record MessageListResponse(List<MessageDto> messageList) {
    }

    public record SearchResponse(MessageDto message) {
    }

    public record SearchNewChatResponse(ChatWithMessagesDto chat, MessageDto message) {
    }

    public record SearchRequest(String searchText) {
    }
}
