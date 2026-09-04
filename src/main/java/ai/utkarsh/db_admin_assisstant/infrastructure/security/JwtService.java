package ai.utkarsh.db_admin_assisstant.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private final String secretKey;
    private final long accessTokenExpiry;
    private final long refreshTokenExpiry;

    public JwtService(@Value("${app.jwt.secret}") String secretKey,
            @Value("${app.jwt.access-token-expiry}") long accessTokenExpiry,
            @Value("${app.jwt.refresh-token-expiry}") long refreshTokenExpiry) {
        this.secretKey = secretKey;
        this.accessTokenExpiry = accessTokenExpiry;
        this.refreshTokenExpiry = refreshTokenExpiry;
    }

    public String generateAccessToken(UserDetails user) {
        String role = user.getAuthorities().stream().findFirst().map(Object::toString).orElse("ROLE_DB_VIEWER");
        return Jwts.builder()
                .claims(Map.of("type", "access", "role", role))
                .subject(user.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiry))
                .signWith(getSigningKey())
                .compact();
    }

    /** Deliberately carries no role claim — a refresh only ever mints a fresh access token (via
     * {@link #generateAccessToken}, which re-reads the user's current authorities), so a role
     * change made while a refresh token is still outstanding takes effect on the very next refresh
     * instead of being frozen at login time for up to 7 days. */
    public String generateRefreshToken(UserDetails user) {
        return Jwts.builder()
                .claims(Map.of("type", "refresh"))
                .subject(user.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiry))
                .signWith(getSigningKey())
                .compact();
    }

    public boolean isAccessTokenValid(String token, UserDetails user) {
        Claims claims = extractClaims(token);
        return "access".equals(claims.get("type", String.class)) && user.getUsername().equals(claims.getSubject())
                && !claims.getExpiration().before(new Date());
    }

    /** Unlike {@link #isAccessTokenValid}, catches parse failures itself — callers hit this
     * directly from a controller (no filter upstream translating a thrown JwtException into a
     * clean 401), so an expired/tampered/malformed refresh token must resolve to "false", not a
     * 500. */
    public boolean isRefreshTokenValid(String token) {
        try {
            Claims claims = extractClaims(token);
            return "refresh".equals(claims.get("type", String.class)) && !claims.getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    private Claims extractClaims(String token) {
        return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }
}
