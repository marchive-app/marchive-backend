package com.marchive.marchive_backend.bookmark.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.List;

public class BookmarkDtos {

    public record MediaItem(
            @JsonProperty("media_type") String mediaType,
            @JsonProperty("ig_cdn_url") String igCdnUrl,
            @JsonProperty("order_index") Integer orderIndex
    ) {
    }

    public record BookmarkItem(
            @JsonProperty("ig_code") String igCode,
            @JsonProperty("author_handle") String authorHandle,
            String caption,
            @JsonProperty("posted_at") OffsetDateTime postedAt,
            @JsonProperty("like_count") Integer likeCount,
            @JsonProperty("media_list") List<MediaItem> mediaList,
            String platform   // 무시
    ) {
    }

    public record BulkRequest(
            @JsonProperty("ig_user_id") String igUserId,
            @JsonProperty("ig_handle") String igHandle,
            List<BookmarkItem> bookmarks
    ) {
    }

    public record BulkResponse(boolean success) {
    }
}
