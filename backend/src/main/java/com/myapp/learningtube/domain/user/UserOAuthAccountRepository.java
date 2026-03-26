package com.myapp.learningtube.domain.user;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserOAuthAccountRepository extends JpaRepository<UserOAuthAccount, Long> {

    /** OAuth 콜백 후 비영속 컨텍스트에서 {@code account.getUser()} 접근 시 LAZY 초기화 오류 방지. */
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

    /** 사용자당 Google 연동 1건 조회(액세스 만료 시 refresh_token 으로 갱신용). */
    Optional<UserOAuthAccount> findFirstByUser_IdAndProviderOrderByUpdatedAtDesc(
            Long userId, OAuthProvider provider);
}
