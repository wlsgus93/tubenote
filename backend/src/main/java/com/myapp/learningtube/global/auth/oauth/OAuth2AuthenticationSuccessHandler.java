package com.myapp.learningtube.global.auth.oauth;

import com.myapp.learningtube.domain.user.OAuthProvider;
import com.myapp.learningtube.domain.user.UserOAuthAccount;
import com.myapp.learningtube.domain.user.UserOAuthAccountRepository;
import com.myapp.learningtube.domain.auth.JwtProperties;
import com.myapp.learningtube.domain.auth.JwtTokenProvider;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final OAuth2LoginProperties properties;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final UserOAuthAccountRepository userOAuthAccountRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;

    public OAuth2AuthenticationSuccessHandler(
            OAuth2LoginProperties properties,
            OAuth2AuthorizedClientService authorizedClientService,
            UserOAuthAccountRepository userOAuthAccountRepository,
            JwtTokenProvider jwtTokenProvider,
            JwtProperties jwtProperties) {
        this.properties = properties;
        this.authorizedClientService = authorizedClientService;
        this.userOAuthAccountRepository = userOAuthAccountRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtProperties = jwtProperties;
    }

    @Override
    public void onAuthenticationSuccess(
            jakarta.servlet.http.HttpServletRequest request,
            jakarta.servlet.http.HttpServletResponse response,
            Authentication authentication)
            throws java.io.IOException {

        if (!(authentication instanceof OAuth2AuthenticationToken token)) {
            response.sendRedirect(buildErrorRedirect("INVALID_AUTH"));
            return;
        }

        if (!(token.getPrincipal() instanceof OidcUser oidcUser)) {
            response.sendRedirect(buildErrorRedirect("OIDC_USER_REQUIRED"));
            return;
        }

        String subject = oidcUser.getSubject();
        Optional<UserOAuthAccount> accountOpt =
                userOAuthAccountRepository.findByProviderAndProviderSubject(OAuthProvider.GOOGLE, subject);
        if (accountOpt.isEmpty()) {
            response.sendRedirect(buildErrorRedirect("OAUTH_ACCOUNT_NOT_FOUND"));
            return;
        }

        UserOAuthAccount account = accountOpt.get();

        // Google access/refresh token 저장 (YouTube API 등에서 사용)
        OAuth2AuthorizedClient client =
                authorizedClientService.loadAuthorizedClient(token.getAuthorizedClientRegistrationId(), token.getName());
        if (client != null && client.getAccessToken() != null) {
            account.setAccessToken(client.getAccessToken().getTokenValue());
            Instant expiresAt = client.getAccessToken().getExpiresAt();
            account.setAccessTokenExpiresAt(expiresAt);
            if (client.getRefreshToken() != null) {
                account.setRefreshToken(client.getRefreshToken().getTokenValue());
            }
            userOAuthAccountRepository.save(account);
        }

        long userId = account.getUser().getId();
        String role = account.getUser().getRole().name();

        String accessJwt = jwtTokenProvider.createAccessToken(userId, role);
        long expiresInSec = Math.max(1L, jwtProperties.getAccessTokenValidityMs() / 1000);

        String redirect =
                properties.getFrontendBaseUrl().replaceAll("/$", "")
                        + "/auth/callback#accessToken="
                        + url(accessJwt)
                        + "&tokenType=Bearer&expiresIn="
                        + expiresInSec;

        response.sendRedirect(redirect);
    }

    private String buildErrorRedirect(String code) {
        return properties.getFrontendBaseUrl().replaceAll("/$", "") + "/login?oauthError=" + url(code);
    }

    private static String url(String v) {
        return URLEncoder.encode(v == null ? "" : v, StandardCharsets.UTF_8);
    }
}

