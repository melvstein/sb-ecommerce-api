package com.melvstein.ecommerce.api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    private SecretKey secretKey;

    @Value("${app.security.jwt.secret-key}")
    private String appSecretKey;

    @Value("${app.security.jwt.timeout}")
    private long timeout;

    @Value("${app.security.jwt.refresh-token.timeout}")
    private long refreshTokenTimeout;

    @PostConstruct
    public void init() {
        // Create key from string (for HMAC algorithms like HS256)
        this.secretKey = Keys.hmacShaKeyFor(appSecretKey.getBytes());
    }

    public long getExpirationTimeMs(long timeout) {
        return timeout * 1000L;
    }

    public long getExpirationTimeSeconds() {
        return timeout;
    }

    public String generateAccessToken(String username, Map<String, Object> extraClaims) {
        return buildToken(username, extraClaims, timeout);
    }

    public String generateRefreshToken(String username) {
        return buildToken(username, new HashMap<>(), refreshTokenTimeout);
    }

    public String buildToken(String username, Map<String, Object> extraClaims, long timeout) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + getExpirationTimeMs(timeout)))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = parseToken(token);
        return claimsResolver.apply(claims);
    }

    public boolean isTokenValid(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
