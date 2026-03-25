package com.myapp.learningtube.global.auth.oauth;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Component
public class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final OAuth2LoginProperties properties;

    public OAuth2AuthenticationFailureHandler(OAuth2LoginProperties properties) {
        this.properties = properties;
    }

    @Override
    public void onAuthenticationFailure(
            jakarta.servlet.http.HttpServletRequest request,
            jakarta.servlet.http.HttpServletResponse response,
            AuthenticationException exception)
            throws java.io.IOException {
        String msg = exception != null ? exception.getClass().getSimpleName() : "OAUTH_FAILED";
        String redirect =
                properties.getFrontendBaseUrl().replaceAll("/$", "")
                        + "/login?oauthError="
                        + URLEncoder.encode(msg, StandardCharsets.UTF_8);
        response.sendRedirect(redirect);
    }
}

