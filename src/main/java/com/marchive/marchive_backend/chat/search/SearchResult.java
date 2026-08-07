package com.marchive.marchive_backend.chat.search;

import com.marchive.marchive_backend.bookmark.domain.Post;
import java.util.List;

public record SearchResult(
        boolean success,
        String assistantContents,
        List<Post> matchedPosts
) {
    
    public static SearchResult failure() {
        return new SearchResult(false, null, List.of());
    }

    public static SearchResult success(String contents, List<Post> posts) {
        return new SearchResult(true, contents, posts);
    }
}
