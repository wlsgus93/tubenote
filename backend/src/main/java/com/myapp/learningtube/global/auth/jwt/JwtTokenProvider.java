package com.myapp.learningtube.global.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    public static final String CLAIM_TYPE = "typ";
    public static final String CLAIM_ROLE = "role";
    public static final String TYPE_ACCESS = "access";

    private final JwtProperties properties;
    private final SecretKey secretKey;

    public JwtTokenProvider(JwtProperties properties) {
        this.properties = properties;
        this.secretKey = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(long userId, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + properties.getAccessTokenValidityMs());
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(CLAIM_TYPE, TYPE_ACCESS)
                .claim(CLAIM_ROLE, role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    public Claims parseAndValidateAccessToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        String typ = claims.get(CLAIM_TYPE, String.class);
        if (!TYPE_ACCESS.equals(typ)) {
            throw new JwtException("Invalid token type");
        }
        return claims;
    }
}
