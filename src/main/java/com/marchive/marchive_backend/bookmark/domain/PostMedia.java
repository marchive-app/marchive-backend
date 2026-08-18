package com.marchive.marchive_backend.bookmark.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "post_media")
public class PostMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_media_id")
    private Long postMediaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false, length = 10)
    private MediaType mediaType;

    @Column(name = "media_url", length = 2000)
    private String mediaUrl;

    @Column(name = "ig_cdn_url", length = 2000)
    private String igCdnUrl;

    @Column(name = "order_index", nullable = false)
    private int orderIndex;

    public enum MediaType {
        image, video
    }

    protected PostMedia() {
    }

    public PostMedia(Post post, MediaType mediaType, String igCdnUrl, int orderIndex) {
        this.post = post;
        this.mediaType = mediaType;
        this.igCdnUrl = igCdnUrl;
        this.orderIndex = orderIndex;
    }

    public Long getPostMediaId() {
        return postMediaId;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public int getOrderIndex() {
        return orderIndex;
    }
}
