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
//        List<BookmarkDto> bookmarks = message.getBookmarks().stream()
//                .map(this::toBookmarkDto)
//                .toList();
        List<BookmarkDto> bookmarks;

        if (message.getRole() == Message.Role.assistant) {
            // UI 확인용 mock 북마크 3개
            bookmarks = mockBookmarks();
        } else {
            // user 메시지는 북마크 없음
            bookmarks = List.of();
        }
        return new MessageDto(
                message.getMessageId(),
                message.getRole().name(),
                message.getContents(),
                bookmarks
        );
    }

    // UI 확인용 가짜 북마크 3개
    private List<BookmarkDto> mockBookmarks() {
        return List.of(
                new BookmarkDto(1L,
                        "https://picsum.photos/300?random=1",
                        "https://instagram.com/p/DbZHxCVk-LS"),
                new BookmarkDto(2L,
                        "https://picsum.photos/300?random=2",
                        "https://instagram.com/p/mockcode2"),
                new BookmarkDto(3L,
                        "https://picsum.photos/300?random=3",
                        "https://instagram.com/p/mockcode3")
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
                mb.getPost().getContentUrl()
        );
    }
}
