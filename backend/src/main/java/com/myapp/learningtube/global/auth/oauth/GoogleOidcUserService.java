package com.myapp.learningtube.global.auth.oauth;

import com.myapp.learningtube.domain.user.OAuthProvider;
import com.myapp.learningtube.domain.user.User;
import com.myapp.learningtube.domain.user.UserOAuthAccount;
import com.myapp.learningtube.domain.user.UserOAuthAccountRepository;
import com.myapp.learningtube.domain.user.UserRepository;
import com.myapp.learningtube.domain.user.UserRole;
import java.util.Optional;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GoogleOidcUserService extends OidcUserService {

    private final UserRepository userRepository;
    private final UserOAuthAccountRepository userOAuthAccountRepository;

    public GoogleOidcUserService(
            UserRepository userRepository, UserOAuthAccountRepository userOAuthAccountRepository) {
        this.userRepository = userRepository;
        this.userOAuthAccountRepository = userOAuthAccountRepository;
    }

    @Override
    @Transactional
    public OidcUser loadUser(OidcUserRequest userRequest) {
        OidcUser oidcUser = super.loadUser(userRequest);

        String subject = oidcUser.getSubject();
        String email = oidcUser.getEmail();
        String name = oidcUser.getFullName();

        if (subject == null || subject.isBlank()) {
            throw new IllegalStateException("Google OIDC subject(sub)가 비어 있습니다.");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalStateException("Google 계정 이메일을 가져올 수 없습니다. scope(email) 설정을 확인해 주세요.");
        }

        Optional<UserOAuthAccount> existing =
                userOAuthAccountRepository.findByProviderAndProviderSubject(OAuthProvider.GOOGLE, subject);

        if (existing.isPresent()) {
            // 로그인만으로는 토큰 저장은 성공 핸들러에서 처리 (authorized client 접근 필요)
            return oidcUser;
        }

        // 신규 연결: users + user_oauth_accounts 생성
        User user = userRepository.findByEmailAndDeletedAtIsNull(email).orElseGet(() -> {
            String nickname = deriveNickname(name, email);
            return userRepository.save(new User(email, null, nickname, UserRole.MEMBER));
        });

        UserOAuthAccount account =
                new UserOAuthAccount(user, OAuthProvider.GOOGLE, subject, email);
        // 토큰들은 success handler에서 저장
        userOAuthAccountRepository.save(account);

        return oidcUser;
    }

    private static String deriveNickname(String fullName, String email) {
        String n = fullName != null ? fullName.trim() : "";
        if (!n.isBlank()) return n.length() > 100 ? n.substring(0, 100) : n;
        String prefix = email != null ? email.split("@", 2)[0] : "user";
        String p = prefix.trim();
        if (p.isBlank()) p = "user";
        return p.length() > 100 ? p.substring(0, 100) : p;
    }
}

