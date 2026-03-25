package com.myapp.learningtube.domain.auth.refresh;

import com.myapp.learningtube.domain.common.BaseEntity;
import com.myapp.learningtube.domain.user.User;
import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Access(AccessType.FIELD)
@Table(
        name = "refresh_tokens",
        indexes = {
            @Index(name = "idx_refresh_tokens_user_revoked", columnList = "user_id, revoked_at"),
            @Index(name = "idx_refresh_tokens_expires_at", columnList = "expires_at"),
        })
public class RefreshToken extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "token_hash", nullable = false, length = 128, unique = true)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    protected RefreshToken() {}

    public RefreshToken(User user, String tokenHash, Instant expiresAt, String userAgent, String ipAddress) {
        this.user = user;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.userAgent = userAgent;
        this.ipAddress = ipAddress;
    }

    public User getUser() {
        return user;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public void revoke(Instant at) {
        this.revokedAt = at;
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }
}

