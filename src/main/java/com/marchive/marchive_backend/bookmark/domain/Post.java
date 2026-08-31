package com.marchive.marchive_backend.bookmark.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long postId;

    @Column(name = "ig_code", nullable = false, unique = true, length = 50)
    private String igCode;

    @Column(name = "author_handle", nullable = false, length = 100)
    private String authorHandle;

    @Column(name = "caption", columnDefinition = "TEXT")
    private String caption;

    @Column(name = "posted_at", nullable = false)
    private LocalDateTime postedAt;

    @Column(name = "like_count", nullable = false)
    private Integer likeCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<PostMedia> mediaList = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    protected Post() {
    }

    public Post(String igCode, String authorHandle, String caption, LocalDateTime postedAt, Integer likeCount) {
        this.igCode = igCode;
        this.authorHandle = authorHandle;
        this.caption = caption;
        this.postedAt = postedAt;
        this.likeCount = likeCount;
    }

    // 원본 게시물 URL은 ig_code로 조합 (저장 안 함)
    public String getContentUrl() {
        return "https://instagram.com/p/" + this.igCode;
    }

    // 대표 썸네일 = 첫 번째 미디어의 media_url
    public String getThumbnailUrl() {
        return mediaList.isEmpty() ? null : mediaList.getFirst().getMediaKey();
    }

    public void addMedia(PostMedia media) {
        this.mediaList.add(media);
    }

    public Long getPostId() {
        return postId;
    }

    public String getIgCode() {
        return igCode;
    }

    public String getAuthorHandle() {
        return authorHandle;
    }

    public String getCaption() {
        return caption;
    }

    public LocalDateTime getPostedAt() {
        return postedAt;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public List<PostMedia> getMediaList() {
        return mediaList;
    }
}
