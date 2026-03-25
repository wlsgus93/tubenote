package com.myapp.learningtube.domain.member;

public enum MemberRole implements Role {
    MEMBER,
    ADMIN;

    @Override
    public String getAuthority() {
        return "ROLE_" + name();
    }
}

