package com.finance.tracker.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {
    private final SecretKey signingKey;
    private final long expirationMinutes;
    private final Clock clock;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration-minutes:60}") long expirationMinutes) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT_SECRET must contain a base64-encoded signing key");
        }
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secret);
        } catch (RuntimeException exception) {
            throw new IllegalStateException("JWT_SECRET must be valid base64", exception);
        }
        if (keyBytes.length < 32) throw new IllegalStateException("JWT_SECRET must decode to at least 32 bytes");
        if (expirationMinutes < 1) throw new IllegalStateException("JWT expiration must be at least one minute");
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMinutes = expirationMinutes;
        this.clock = Clock.systemUTC();
    }

    public IssuedToken issue(UserDetails userDetails) {
        Instant issuedAt = clock.instant();
        Instant expiresAt = issuedAt.plusSeconds(expirationMinutes * 60);
        String token = Jwts.builder()
                .subject(userDetails.getUsername())
                .claims(Map.of("roles", userDetails.getAuthorities().stream().map(a -> a.getAuthority()).toList()))
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey)
                .compact();
        return new IssuedToken(token, expiresAt);
    }

    public String extractUsername(String token) {
        return claims(token).getSubject();
    }

    public boolean isValid(String token) {
        try {
            return claims(token).getExpiration().after(Date.from(clock.instant()));
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    private Claims claims(String token) {
        return Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
    }

    public record IssuedToken(String value, Instant expiresAt) { }
}
