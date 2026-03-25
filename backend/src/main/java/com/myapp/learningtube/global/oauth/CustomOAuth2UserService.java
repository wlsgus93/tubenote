package com.myapp.learningtube.global.oauth;

import com.myapp.learningtube.domain.member.Member;
import com.myapp.learningtube.domain.member.MemberRepository;
import com.myapp.learningtube.domain.member.MemberRole;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Google OAuth2 로그인 성공 시 사용자 정보를 DB에 Upsert 하는 서비스.
 *
 * <p>민감정보(토큰 원문)는 저장/로그하지 않는다.</p>
 */
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger log = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    private final MemberRepository memberRepository;

    public CustomOAuth2UserService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> a = oAuth2User.getAttributes();

        String email = asString(a.get("email"));
        final String rawName = asString(a.get("name"));
        final String picture = asString(a.get("picture"));

        if (email == null || email.isBlank()) {
            // Google 기본 scope(email)로는 항상 포함되는 편이지만, 방어적으로 처리.
            throw new OAuth2AuthenticationException("Missing email from OAuth2 provider");
        }
        final String name = (rawName == null || rawName.isBlank()) ? email : rawName;

        Member member =
                memberRepository
                        .findById(email)
                        .map(
                                existing -> {
                                    existing.updateProfile(name, picture);
                                    return existing;
                                })
                        .orElseGet(() -> new Member(email, name, picture, MemberRole.MEMBER));

        memberRepository.save(member);

        // 토큰/개인정보 과다 로깅 금지: 최소한의 식별만 남김
        log.info("OAuth2 login upsert memberEmail={}", email);

        return new DefaultOAuth2User(
                java.util.List.of(new SimpleGrantedAuthority(member.getRole().getAuthority())),
                a,
                "email");
    }

    private static String asString(Object v) {
        return v == null ? null : String.valueOf(v);
    }
}

