package com.myapp.learningtube.domain.user;

import com.myapp.learningtube.domain.common.BaseEntity;
import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Access(AccessType.FIELD)
@Table(
        name = "users",
        indexes = {@Index(name = "idx_users_deleted_at", columnList = "deleted_at")})
public class User extends BaseEntity {

    @Column(nullable = false, length = 320)
    private String email;

    /**
     * 로컬 비밀번호 로그인용. OAuth 전용 계정은 null 가능.
     * (PostgreSQL DDL의 NOT NULL은 OAuth 도입 시 마이그레이션으로 완화)
     */
    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(nullable = false, length = 100)
    private String nickname;

    @Column(name = "profile_image_url", length = 2048)
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private UserRole role = UserRole.MEMBER;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    protected User() {}

    public User(String email, String passwordHash, String nickname, UserRole role) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.nickname = nickname;
        this.role = role != null ? role : UserRole.MEMBER;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getNickname() {
        return nickname;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public UserRole getRole() {
        return role;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void softDelete(Instant at) {
        this.deletedAt = at;
    }

    public void restore() {
        this.deletedAt = null;
    }
}
