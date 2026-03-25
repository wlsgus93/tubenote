package com.myapp.learningtube.global.auth.oauth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

/**
 * {@code SessionCreationPolicy.STATELESS} 환경에서 OAuth2 로그인 시작 시 state/nonce 등을
 * 세션 대신 쿠키에 보관한다. 기본 {@code HttpSessionOAuth2AuthorizationRequestRepository}와
 * 조합 시 일부 환경에서 인가 요청 단계에서 500이 나는 경우가 있어 분리한다.
 */
@Component
public class CookieOAuth2AuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    static final String OAUTH_COOKIE_NAME = "oauth2_authorization_request";
    private static final int COOKIE_MAX_AGE_SEC = 600;

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        return getCookie(request, OAUTH_COOKIE_NAME).map(this::deserialize).orElse(null);
    }

    @Override
    public void saveAuthorizationRequest(
            OAuth2AuthorizationRequest authorizationRequest,
            HttpServletRequest request,
            HttpServletResponse response) {
        if (authorizationRequest == null) {
            deleteCookie(request, response, OAUTH_COOKIE_NAME);
            return;
        }
        addCookie(request, response, OAUTH_COOKIE_NAME, serialize(authorizationRequest), COOKIE_MAX_AGE_SEC);
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(
            HttpServletRequest request, HttpServletResponse response) {
        OAuth2AuthorizationRequest existing = loadAuthorizationRequest(request);
        deleteCookie(request, response, OAUTH_COOKIE_NAME);
        return existing;
    }

    private static String serialize(OAuth2AuthorizationRequest request) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(request);
            }
            return Base64.getUrlEncoder().withoutPadding().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            throw new IllegalStateException("OAuth2 authorization request 직렬화 실패", e);
        }
    }

    private OAuth2AuthorizationRequest deserialize(String encoded) {
        try {
            byte[] bytes = Base64.getUrlDecoder().decode(encoded);
            try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
                Object o = ois.readObject();
                if (o instanceof OAuth2AuthorizationRequest r) {
                    return r;
                }
            }
        } catch (IOException | ClassNotFoundException ignored) {
            // 잘못된/만료 쿠키
        }
        return null;
    }

    private static Optional<String> getCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }
        return Arrays.stream(request.getCookies())
                .filter(c -> name.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }

    private static void addCookie(
            HttpServletRequest request,
            HttpServletResponse response,
            String name,
            String value,
            int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(maxAge);
        cookie.setSecure(request.isSecure());
        response.addCookie(cookie);
    }

    private static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        cookie.setSecure(request.isSecure());
        response.addCookie(cookie);
    }
}
