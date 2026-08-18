package com.marchive.marchive_backend.bookmark.repository;

import com.marchive.marchive_backend.bookmark.domain.Bookmark;
import com.marchive.marchive_backend.bookmark.domain.Post;
import com.marchive.marchive_backend.igaccount.domain.IgAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    boolean existsByIgAccountAndPost(IgAccount igAccount, Post post);
}
