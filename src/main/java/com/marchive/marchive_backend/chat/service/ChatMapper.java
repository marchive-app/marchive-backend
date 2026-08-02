package com.marchive.marchive_backend.chat.service;

import com.marchive.marchive_backend.chat.domain.Message;
import com.marchive.marchive_backend.chat.domain.MessageBookmark;
import com.marchive.marchive_backend.chat.dto.ChatDtos.BookmarkDto;
import com.marchive.marchive_backend.chat.dto.ChatDtos.MessageDto;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ChatMapper {

    public MessageDto toMessageDto(Message message) {
        List<BookmarkDto> bookmarks = message.getBookmarks().stream()
                .map(this::toBookmarkDto)
                .toList();

        return new MessageDto(
                message.getMessageId(),
                message.getRole().name(),
                message.getContents(),
                bookmarks
        );
    }

    private BookmarkDto toBookmarkDto(MessageBookmark mb) {
        return new BookmarkDto(
                mb.getMessageBookmarkId(),
                mb.getPost().getThumbnailUrl(),
                mb.getPost().getContentUrl()
        );
    }
}
