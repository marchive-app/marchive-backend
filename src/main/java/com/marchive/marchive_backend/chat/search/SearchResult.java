package com.marchive.marchive_backend.chat.search;

import com.marchive.marchive_backend.bookmark.domain.Post;
import java.util.List;

public record SearchResult(String assistantContents, List<Post> matchedPosts) {
}
