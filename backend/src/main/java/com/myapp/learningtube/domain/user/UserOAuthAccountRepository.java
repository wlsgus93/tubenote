package com.myapp.learningtube.domain.user;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserOAuthAccountRepository extends JpaRepository<UserOAuthAccount, Long> {

    /**
     * OAuth 성공 핸들러에서 {@code account.getUser().getId()} 호출 시 LAZY 로 500 방지(open-in-view false).
     */
    @EntityGraph(attributePaths = "user")
    Optional<UserOAuthAccount> findByProviderAndProviderSubject(
            OAuthProvider provider, String providerSubject);

    List<UserOAuthAccount> findByUser(User user);

    boolean existsByProviderAndProviderSubject(OAuthProvider provider, String providerSubject);

    /**
     * YouTube Data API용: Google 연동 계정 중 액세스 토큰이 있는 가장 최근 갱신 행.
     */
    Optional<UserOAuthAccount> findFirstByUser_IdAndProviderAndAccessTokenIsNotNullOrderByUpdatedAtDesc(
            Long userId, OAuthProvider provider);
}
