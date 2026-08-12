package com.marchive.marchive_backend.igaccount.domain;

import com.marchive.marchive_backend.auth.domain.User;
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
@Table(name = "ig_accounts")
public class IgAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ig_account_id")
    private Long igAccountId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "ig_user_id", nullable = false, unique = true, length = 50)
    private String igUserId;

    @Column(name = "ig_handle", nullable = false, length = 100)
    private String igHandle;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    protected IgAccount() {
    }

    public IgAccount(User user, String igUserId, String igHandle) {
        this.user = user;
        this.igUserId = igUserId;
        this.igHandle = igHandle;
    }

    public void updateHandle(String igHandle) {
        this.igHandle = igHandle;
    }

    public Long getIgAccountId() {
        return igAccountId;
    }

    public String getIgHandle() {
        return igHandle;
    }

    public String getIgUserId() {
        return igUserId;
    }

    public User getUser() {
        return user;
    }
}