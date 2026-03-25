package com.myapp.learningtube.domain.user;

import com.myapp.learningtube.domain.common.BaseEntity;
import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

@Entity
@Access(AccessType.FIELD)
@Table(
        name = "user_oauth_accounts",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uk_user_oauth_provider_subject",
                        columnNames = {"provider", "provider_subject"}),
        indexes = {@Index(name = "idx_user_oauth_user_id", columnList = "user_id")})
public class UserOAuthAccount extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private OAuthProvider provider;

    /** OIDC sub 등 제공자 측 불변 식별자. */
    @Column(name = "provider_subject", nullable = false, length = 255)
    private String providerSubject;

    /** 연동 시점 스냅샷(로그인 식별 보조, 검증용 아님). */
    @Column(name = "provider_email", length = 320)
    private String providerEmail;

    /**
     * Google OAuth access token (YouTube Data API 등). 운영에서는 암호화·Vault 권장. 로그 금지.
     */
    @Column(name = "access_token", columnDefinition = "TEXT")
    private String accessToken;

    @Column(name = "refresh_token", columnDefinition = "TEXT")
    private String refreshToken;

    @Column(name = "access_token_expires_at")
    private Instant accessTokenExpiresAt;

    protected UserOAuthAccount() {}

    public UserOAuthAccount(User user, OAuthProvider provider, String providerSubject, String providerEmail) {
        this.user = user;
        this.provider = provider;
        this.providerSubject = providerSubject;
        this.providerEmail = providerEmail;
    }

    public User getUser() {
        return user;
    }

    public OAuthProvider getProvider() {
        return provider;
    }

    public String getProviderSubject() {
        return providerSubject;
    }

    public String getProviderEmail() {
        return providerEmail;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Instant getAccessTokenExpiresAt() {
        return accessTokenExpiresAt;
    }

    public void setAccessTokenExpiresAt(Instant accessTokenExpiresAt) {
        this.accessTokenExpiresAt = accessTokenExpiresAt;
    }
}
