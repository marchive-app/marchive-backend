package com.marchive.marchive_backend.bookmark.domain;

import com.marchive.marchive_backend.igaccount.domain.IgAccount;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookmarks")
public class Bookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bookmark_id")
    private Long bookmarkId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ig_account_id", nullable = false)
    private IgAccount igAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "bookmarked_at", nullable = false)
    private LocalDateTime bookmarkedAt;

    @PrePersist
    protected void onCreate() {
        this.bookmarkedAt = LocalDateTime.now();
    }

    protected Bookmark() {
    }

    public Bookmark(IgAccount igAccount, Post post) {
        this.igAccount = igAccount;
        this.post = post;
    }

    public Long getBookmarkId() {
        return bookmarkId;
    }

    public Post getPost() {
        return post;
    }
}
