package com.myapp.learningtube.domain.member;

/**
 * 권한(역할) 표현용 인터페이스.
 *
 * <p>Spring Security의 GrantedAuthority 문자열(예: ROLE_MEMBER)을 제공한다.</p>
 */
public interface Role {

    String getAuthority();
}

