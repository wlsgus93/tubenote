package com.myapp.learningtube.domain.auth.google;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.myapp.learningtube.global.error.BusinessException;
import com.myapp.learningtube.global.error.ErrorCode;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class GoogleIdTokenVerifierService {

    private static final List<String> ALLOWED_ISSUERS =
            List.of("https://accounts.google.com", "accounts.google.com");

    private final GoogleIdTokenVerifier verifier;

    public GoogleIdTokenVerifierService(GoogleAuthProperties properties) {
        if (!StringUtils.hasText(properties.getClientId())) {
            throw new IllegalStateException("google.auth.client-id must be configured");
        }
        this.verifier =
                new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance())
                        .setAudience(List.of(properties.getClientId()))
                        .setIssuers(ALLOWED_ISSUERS)
                        .build();
    }

    public GoogleIdToken.Payload verifyOrThrow(String idToken) {
        if (!StringUtils.hasText(idToken)) {
            throw new BusinessException(ErrorCode.GOOGLE_ID_TOKEN_INVALID, "Google ID token이 필요합니다.");
        }
        try {
            GoogleIdToken token = verifier.verify(idToken);
            if (token == null) {
                throw new BusinessException(ErrorCode.GOOGLE_ID_TOKEN_INVALID, "유효하지 않은 Google ID token입니다.");
            }
            return token.getPayload();
        } catch (GeneralSecurityException | IOException e) {
            throw new BusinessException(ErrorCode.GOOGLE_ID_TOKEN_INVALID, "Google ID token 검증에 실패했습니다.");
        }
    }
}

