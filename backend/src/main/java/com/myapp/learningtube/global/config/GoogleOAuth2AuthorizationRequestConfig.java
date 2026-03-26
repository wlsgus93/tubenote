package com.myapp.learningtube.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;

/**
 * Google OAuth 에 {@code refresh_token} 을 받기 위한 파라미터.
 * 없으면 액세스 토큰 만료 후 YouTube API 호출이 막히기 쉽다.
 */
@Configuration
public class GoogleOAuth2AuthorizationRequestConfig {

    private static final String AUTHORIZATION_BASE_URI = "/oauth2/authorization";

    @Bean
    public OAuth2AuthorizationRequestResolver oauth2AuthorizationRequestResolver(
            ClientRegistrationRepository clientRegistrationRepository) {
        DefaultOAuth2AuthorizationRequestResolver resolver =
                new DefaultOAuth2AuthorizationRequestResolver(
                        clientRegistrationRepository, AUTHORIZATION_BASE_URI);
        resolver.setAuthorizationRequestCustomizer(
                customizer ->
                        customizer.additionalParameters(
                                params -> {
                                    params.put("access_type", "offline");
                                    params.put("prompt", "consent");
                                }));
        return resolver;
    }
}
