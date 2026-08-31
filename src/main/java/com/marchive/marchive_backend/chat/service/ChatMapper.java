package com.marchive.marchive_backend.chat.service;

import com.marchive.marchive_backend.bookmark.domain.Post;
import com.marchive.marchive_backend.bookmark.domain.PostMedia;
import com.marchive.marchive_backend.chat.domain.Message;
import com.marchive.marchive_backend.chat.domain.MessageBookmark;
import com.marchive.marchive_backend.chat.dto.ChatDtos.BookmarkDto;
import com.marchive.marchive_backend.chat.dto.ChatDtos.MessageDto;
import com.marchive.marchive_backend.global.s3.S3Service;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ChatMapper {

    private final S3Service s3Service;

    public ChatMapper(S3Service s3Service) {
        this.s3Service = s3Service;
    }

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
        Post post = mb.getPost();
        PostMedia firstMedia = post.getMediaList().isEmpty()
                ? null
                : post.getMediaList().getFirst();

        String thumbnailUrl = (firstMedia != null && firstMedia.getUploadStatus() == PostMedia.UploadStatus.DONE)
                ? s3Service.generatePresignedUrl(firstMedia.getMediaKey())
                : null;

        return new BookmarkDto(
                mb.getMessageBookmarkId(),
                thumbnailUrl,
                post.getContentUrl()
        );
    }
}
