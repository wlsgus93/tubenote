package com.myapp.learningtube.domain.auth.google;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myapp.learningtube.domain.user.OAuthProvider;
import com.myapp.learningtube.domain.user.UserOAuthAccount;
import com.myapp.learningtube.domain.user.UserOAuthAccountRepository;
import com.myapp.learningtube.global.error.BusinessException;
import com.myapp.learningtube.global.error.ErrorCode;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

/**
 * YouTube Data API 등에 쓸 Google OAuth 액세스 토큰 — 만료 임박 시 {@code refresh_token} 으로 갱신 후 DB 반영.
 */
@Service
public class GoogleOAuthAccessTokenService {

    private static final Logger log = LoggerFactory.getLogger(GoogleOAuthAccessTokenService.class);
    private static final long EXPIRY_SKEW_SEC = 120;

    private final UserOAuthAccountRepository userOAuthAccountRepository;
    private final GoogleAuthProperties googleAuthProperties;
    private final ObjectMapper objectMapper;
    private final RestClient tokenClient;

    public GoogleOAuthAccessTokenService(
            UserOAuthAccountRepository userOAuthAccountRepository,
            GoogleAuthProperties googleAuthProperties,
            ObjectMapper objectMapper) {
        this.userOAuthAccountRepository = userOAuthAccountRepository;
        this.googleAuthProperties = googleAuthProperties;
        this.objectMapper = objectMapper;
        this.tokenClient = RestClient.builder().baseUrl("https://oauth2.googleapis.com").build();
    }

    /**
     * 유효한 액세스 토큰(가능하면 갱신 후). 없거나 갱신 불가면 empty.
     */
    @Transactional
    public Optional<String> resolveValidAccessToken(Long userId) {
        Optional<UserOAuthAccount> accOpt =
                userOAuthAccountRepository.findFirstByUser_IdAndProviderOrderByUpdatedAtDesc(
                        userId, OAuthProvider.GOOGLE);
        if (accOpt.isEmpty()) {
            return Optional.empty();
        }
        UserOAuthAccount account = accOpt.get();
        if (needsRefresh(account)) {
            refreshAccessToken(account);
            account = userOAuthAccountRepository.save(account);
        }
        String t = account.getAccessToken();
        if (t == null || t.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(t);
    }

    private static boolean needsRefresh(UserOAuthAccount account) {
        String access = account.getAccessToken();
        if (access == null || access.isBlank()) {
            return true;
        }
        Instant exp = account.getAccessTokenExpiresAt();
        if (exp == null) {
            return true;
        }
        return !Instant.now().isBefore(exp.minusSeconds(EXPIRY_SKEW_SEC));
    }

    private void refreshAccessToken(UserOAuthAccount account) {
        String refresh = account.getRefreshToken();
        if (refresh == null || refresh.isBlank()) {
            throw new BusinessException(
                    ErrorCode.YOUTUBE_AUTH_FAILED,
                    "Google 액세스 토큰이 갱신되지 않았습니다. refresh_token 이 없으면 다시 로그인해 주세요.");
        }
        String clientId = googleAuthProperties.getClientId();
        String clientSecret = googleAuthProperties.getClientSecret();
        if (clientId == null
                || clientId.isBlank()
                || clientSecret == null
                || clientSecret.isBlank()) {
            throw new BusinessException(
                    ErrorCode.YOUTUBE_ACCESS_TOKEN_MISSING,
                    "액세스 토큰 갱신을 위해 google.auth.client-id 와 google.auth.client-secret 가 필요합니다.");
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "refresh_token");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("refresh_token", refresh);

        String body;
        try {
            body =
                    tokenClient
                            .post()
                            .uri("/token")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .body(form)
                            .retrieve()
                            .onStatus(
                                    status -> status.is4xxClientError() || status.is5xxServerError(),
                                    (req, res) -> {
                                        String err = readBodyString(res.getBody());
                                        log.warn(
                                                "google token refresh HTTP {} body={}",
                                                res.getStatusCode().value(),
                                                truncate(err, 400));
                                        String lower = err.toLowerCase();
                                        if (lower.contains("invalid_grant")) {
                                            throw new BusinessException(
                                                    ErrorCode.YOUTUBE_AUTH_FAILED,
                                                    "Google 연동이 만료되었습니다. 다시 로그인해 주세요.");
                                        }
                                        throw new BusinessException(
                                                ErrorCode.YOUTUBE_UPSTREAM_ERROR,
                                                "Google 액세스 토큰을 갱신하지 못했습니다. 잠시 후 다시 시도해 주세요.");
                                    })
                            .body(String.class);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("google token refresh request failed: {}", e.getMessage());
            throw new BusinessException(
                    ErrorCode.YOUTUBE_UPSTREAM_ERROR,
                    "Google 액세스 토큰을 갱신하지 못했습니다. 잠시 후 다시 시도해 주세요.");
        }

        try {
            JsonNode root = objectMapper.readTree(body);
            String accessToken = root.path("access_token").asText(null);
            if (accessToken == null || accessToken.isBlank()) {
                throw new BusinessException(
                        ErrorCode.YOUTUBE_UPSTREAM_ERROR, "Google 토큰 응답에 access_token 이 없습니다.");
            }
            account.setAccessToken(accessToken);
            long expiresIn = root.path("expires_in").asLong(3600);
            account.setAccessTokenExpiresAt(Instant.now().plusSeconds(Math.max(60, expiresIn)));
            String newRefresh = root.path("refresh_token").asText(null);
            if (newRefresh != null && !newRefresh.isBlank()) {
                account.setRefreshToken(newRefresh);
            }
            log.info("google access token refreshed userOAuthAccountId={}", account.getId());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("google token response parse failed: {}", e.getMessage());
            throw new BusinessException(
                    ErrorCode.YOUTUBE_UPSTREAM_ERROR, "Google 토큰 응답을 해석할 수 없습니다.");
        }
    }

    private static String readBodyString(java.io.InputStream in) {
        if (in == null) {
            return "";
        }
        try {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "";
        }
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        return s.length() <= max ? s : s.substring(0, max) + "…";
    }
}
