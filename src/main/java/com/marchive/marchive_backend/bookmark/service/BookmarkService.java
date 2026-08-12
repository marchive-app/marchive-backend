package com.marchive.marchive_backend.bookmark.service;

import com.marchive.marchive_backend.bookmark.domain.Bookmark;
import com.marchive.marchive_backend.bookmark.domain.Post;
import com.marchive.marchive_backend.bookmark.domain.PostMedia;
import com.marchive.marchive_backend.bookmark.dto.BookmarkDtos.BookmarkItem;
import com.marchive.marchive_backend.bookmark.dto.BookmarkDtos.BulkRequest;
import com.marchive.marchive_backend.bookmark.dto.BookmarkDtos.BulkResponse;
import com.marchive.marchive_backend.bookmark.dto.BookmarkDtos.MediaItem;
import com.marchive.marchive_backend.bookmark.repository.BookmarkRepository;
import com.marchive.marchive_backend.bookmark.repository.PostRepository;
import com.marchive.marchive_backend.igaccount.domain.IgAccount;
import com.marchive.marchive_backend.igaccount.service.IgAccountService;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookmarkService {

    private final IgAccountService igAccountService;
    private final PostRepository postRepository;
    private final BookmarkRepository bookmarkRepository;

    public BookmarkService(
            IgAccountService igAccountService,
            PostRepository postRepository,
            BookmarkRepository bookmarkRepository
    ) {
        this.igAccountService = igAccountService;
        this.postRepository = postRepository;
        this.bookmarkRepository = bookmarkRepository;
    }

    @Transactional
    public BulkResponse saveBulk(Long userId, BulkRequest request) {
        IgAccount igAccount = igAccountService.getLinkedAccount(userId, request.igUserId());

        for (BookmarkItem item : request.bookmarks()) {
            // ig_code로 중복 확인 → 이미 있으면 재사용, 없으면 새로 저장
            Post post = postRepository.findByIgCode(item.igCode())
                    .orElseGet(() -> createPost(item));

            // 같은 계정이 같은 게시물을 이미 북마크했으면 건너뜀
            if (!bookmarkRepository.existsByIgAccountAndPost(igAccount, post)) {
                bookmarkRepository.save(new Bookmark(igAccount, post));
            }
        }

        return new BulkResponse(true);
    }

    private Post createPost(BookmarkItem item) {
        LocalDateTime postedAt = item.postedAt()
                .atZoneSameInstant(ZoneId.of("Asia/Seoul"))
                .toLocalDateTime();

        Post post = new Post(
                item.igCode(),
                item.authorHandle(),
                item.caption(),
                postedAt,
                item.likeCount() != null ? item.likeCount() : 0
        );

        if (item.mediaList() != null) {
            for (MediaItem media : item.mediaList()) {
                post.addMedia(new PostMedia(
                        post,
                        PostMedia.MediaType.valueOf(media.mediaType()),   // "image"/"video"
                        media.igCdnUrl(),
                        media.orderIndex() != null ? media.orderIndex() : 0
                ));
            }
        }

        return postRepository.save(post);
    }
}
