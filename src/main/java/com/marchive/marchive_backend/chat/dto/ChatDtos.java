package com.marchive.marchive_backend.chat.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record SearchResponse(
            boolean success,
            MessageDto searchMessage, // 항상 포함
            MessageDto responseMessage // 실패 시 null → JSON에서 제외
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record SearchNewChatResponse(
            ChatWithMessagesDto chat,
            boolean success,
            MessageDto searchMessage,
            MessageDto responseMessage
    ) {
    }

    public record SearchRequest(
            @JsonProperty("ig_account_id") Long igAccountId,
            String searchText
    ) {
    }
}
