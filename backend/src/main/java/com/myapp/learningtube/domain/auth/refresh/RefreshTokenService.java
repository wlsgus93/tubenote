package com.myapp.learningtube.domain.auth.refresh;

import com.myapp.learningtube.domain.user.User;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class RefreshTokenService {

    private static final Duration DEFAULT_TTL = Duration.ofDays(7);

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public IssuedRefreshToken issue(User user, String userAgent, String ipAddress) {
        String raw = generateOpaqueToken();
        String hash = sha256Hex(raw);
        Instant expiresAt = Instant.now().plus(DEFAULT_TTL);
        refreshTokenRepository.save(new RefreshToken(user, hash, expiresAt, userAgent, ipAddress));
        return new IssuedRefreshToken(raw, expiresAt);
    }

    private static String generateOpaqueToken() {
        // UUID 기반 + base64url 인코딩: 예측 불가 수준 확보(MVP). 추후 SecureRandom 32B로 교체 가능.
        String seed = UUID.randomUUID() + ":" + UUID.randomUUID();
        return Base64.getUrlEncoder().withoutPadding().encodeToString(seed.getBytes(StandardCharsets.UTF_8));
    }

    public static String sha256Hex(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    public record IssuedRefreshToken(String refreshToken, Instant expiresAt) {}
}

