package com.myapp.learningtube.domain.auth.google;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.myapp.learningtube.domain.auth.JwtTokenProvider;
import com.myapp.learningtube.domain.auth.dto.AuthUserResponse;
import com.myapp.learningtube.domain.auth.dto.GoogleLoginResponse;
import com.myapp.learningtube.domain.auth.refresh.RefreshTokenService;
import com.myapp.learningtube.domain.user.OAuthProvider;
import com.myapp.learningtube.domain.user.User;
import com.myapp.learningtube.domain.user.UserOAuthAccount;
import com.myapp.learningtube.domain.user.UserOAuthAccountRepository;
import com.myapp.learningtube.domain.user.UserRepository;
import com.myapp.learningtube.domain.user.UserRole;
import com.myapp.learningtube.global.error.BusinessException;
import com.myapp.learningtube.global.error.ErrorCode;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class GoogleLoginService {

    private static final Logger log = LoggerFactory.getLogger(GoogleLoginService.class);

    private final GoogleIdTokenVerifierService googleIdTokenVerifierService;
    private final UserRepository userRepository;
    private final UserOAuthAccountRepository userOAuthAccountRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    public GoogleLoginService(
            GoogleIdTokenVerifierService googleIdTokenVerifierService,
            UserRepository userRepository,
            UserOAuthAccountRepository userOAuthAccountRepository,
            JwtTokenProvider jwtTokenProvider,
            RefreshTokenService refreshTokenService) {
        this.googleIdTokenVerifierService = googleIdTokenVerifierService;
        this.userRepository = userRepository;
        this.userOAuthAccountRepository = userOAuthAccountRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public GoogleLoginResponse login(String idToken, String userAgent, String ipAddress) {
        GoogleIdToken.Payload payload = googleIdTokenVerifierService.verifyOrThrow(idToken);

        String sub = payload.getSubject();
        String email = payload.getEmail();
        Boolean emailVerified = payload.getEmailVerified();

        if (!StringUtils.hasText(email)) {
            throw new BusinessException(ErrorCode.GOOGLE_EMAIL_MISSING, "Google 계정 이메일 정보를 확인할 수 없습니다.");
        }
        if (emailVerified != null && !emailVerified) {
            throw new BusinessException(ErrorCode.GOOGLE_EMAIL_NOT_VERIFIED, "이메일 인증이 완료된 Google 계정만 로그인할 수 있습니다.");
        }
        if (!StringUtils.hasText(sub)) {
            throw new BusinessException(ErrorCode.GOOGLE_ID_TOKEN_INVALID, "유효하지 않은 Google ID token입니다.");
        }

        OAuthProvider provider = OAuthProvider.GOOGLE;
        Optional<UserOAuthAccount> existingAccount =
                userOAuthAccountRepository.findByProviderAndProviderSubject(provider, sub);

        User user;
        boolean createdUser = false;
        boolean createdLink = false;

        if (existingAccount.isPresent()) {
            UserOAuthAccount account = existingAccount.get();
            user = account.getUser();
            // providerEmail 스냅샷 갱신(검증용 아님)
            if (!email.equals(account.getProviderEmail())) {
                // 엔티티에 setter가 없어, 스냅샷 갱신은 MVP에서 생략(필요 시 엔티티 메서드 추가)
            }
        } else {
            user = userRepository.findByEmailAndDeletedAtIsNull(email).orElse(null);
            if (user == null) {
                String nickname = resolveNickname(payload, email);
                user = new User(email, null, nickname, UserRole.MEMBER);
                String picture = (String) payload.get("picture");
                if (StringUtils.hasText(picture)) {
                    user.setProfileImageUrl(picture);
                }
                user = userRepository.save(user);
                createdUser = true;
            } else {
                String picture = (String) payload.get("picture");
                if (user.getProfileImageUrl() == null && StringUtils.hasText(picture)) {
                    user.setProfileImageUrl(picture);
                    user = userRepository.save(user);
                }
            }

            UserOAuthAccount link = new UserOAuthAccount(user, provider, sub, email);
            userOAuthAccountRepository.save(link);
            createdLink = true;
        }

        if (createdUser) {
            log.info("Google signup userId={} providerSubjectPrefix={}", user.getId(), maskSubject(sub));
        } else if (createdLink) {
            log.info("Google link-created userId={} providerSubjectPrefix={}", user.getId(), maskSubject(sub));
        } else {
            log.info("Google login userId={} providerSubjectPrefix={}", user.getId(), maskSubject(sub));
        }

        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getRole().name());
        RefreshTokenService.IssuedRefreshToken issuedRefresh = refreshTokenService.issue(user, userAgent, ipAddress);

        AuthUserResponse userDto =
                new AuthUserResponse(
                        user.getId(),
                        user.getEmail(),
                        user.getNickname(),
                        user.getProfileImageUrl());
        return new GoogleLoginResponse(accessToken, issuedRefresh.refreshToken(), userDto);
    }

    private static String resolveNickname(GoogleIdToken.Payload payload, String email) {
        String name = (String) payload.get("name");
        if (StringUtils.hasText(name)) {
            return truncate(name.trim(), 100);
        }
        String givenName = (String) payload.get("given_name");
        if (StringUtils.hasText(givenName)) {
            return truncate(givenName.trim(), 100);
        }
        int at = email.indexOf('@');
        String local = at > 0 ? email.substring(0, at) : email;
        return truncate(local, 100);
    }

    private static String truncate(String s, int maxLen) {
        if (s == null) {
            return null;
        }
        if (s.length() <= maxLen) {
            return s;
        }
        return s.substring(0, maxLen);
    }

    private static String maskSubject(String sub) {
        if (sub == null || sub.length() < 6) {
            return "******";
        }
        return sub.substring(0, 6) + "…";
    }
}

