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

    @Column(name = "media_key", length = 2000)
    private String mediaKey;

    @Column(name = "ig_cdn_url", length = 2000)
    private String igCdnUrl;

    @Column(name = "order_index", nullable = false)
    private int orderIndex;

    @Enumerated(EnumType.STRING)
    @Column(name = "upload_status", nullable = false, length = 20)
    private UploadStatus uploadStatus = UploadStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "ocr_status", nullable = false, length = 20)
    private OcrStatus ocrStatus = OcrStatus.PENDING;

    @Column(name = "ocr_text", columnDefinition = "TEXT")
    private String ocrText;

    public enum MediaType {
        image, video
    }

    public enum UploadStatus {
        PENDING, PROCESSING, DONE, FAILED
    }

    public enum OcrStatus {PENDING, PROCESSING, DONE, FAILED}

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

    public String getMediaKey() {
        return mediaKey;
    }

    public void updateMediaKey(String mediaKey) {
        this.mediaKey = mediaKey;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public UploadStatus getUploadStatus() {
        return uploadStatus;
    }

    public OcrStatus getOcrStatus() {
        return ocrStatus;
    }

    public String getOcrText() {
        return ocrText;
    }
}
