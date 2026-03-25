package com.myapp.learningtube.domain.member;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Google OAuth2 로그인 기준의 회원 엔티티.
 *
 * <p>요구사항에 따라 email을 PK로 사용한다.</p>
 */
@Entity
@Access(AccessType.FIELD)
@Table(name = "members")
public class Member {

    @Id
    @Column(nullable = false, length = 320)
    private String email;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 2048)
    private String picture;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private MemberRole role = MemberRole.MEMBER;

    protected Member() {}

    public Member(String email, String name, String picture, MemberRole role) {
        this.email = email;
        this.name = name;
        this.picture = picture;
        this.role = role != null ? role : MemberRole.MEMBER;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getPicture() {
        return picture;
    }

    public MemberRole getRole() {
        return role;
    }

    public void updateProfile(String name, String picture) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (picture != null && !picture.isBlank()) {
            this.picture = picture;
        }
    }
}

