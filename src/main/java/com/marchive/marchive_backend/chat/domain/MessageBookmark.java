package com.marchive.marchive_backend.chat.domain;

import com.marchive.marchive_backend.bookmark.domain.Post;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "message_bookmarks")
public class MessageBookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_bookmark_id")
    private Long messageBookmarkId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "order_index", nullable = false)
    private int orderIndex;

    protected MessageBookmark() {
    }

    public MessageBookmark(Message message, Post post, int orderIndex) {
        this.message = message;
        this.post = post;
        this.orderIndex = orderIndex;
    }

    public Long getMessageBookmarkId() {
        return messageBookmarkId;
    }

    public Post getPost() {
        return post;
    }

    public int getOrderIndex() {
        return orderIndex;
    }
}
