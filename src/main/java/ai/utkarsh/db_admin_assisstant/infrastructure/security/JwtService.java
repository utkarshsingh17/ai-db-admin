package ai.utkarsh.db_admin_assisstant.infrastructure.security;

import io.jsonwebtoken.Claims;
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

    public JwtService(@Value("${app.jwt.secret}") String secretKey,
            @Value("${app.jwt.access-token-expiry}") long accessTokenExpiry) {
        this.secretKey = secretKey;
        this.accessTokenExpiry = accessTokenExpiry;
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

    public boolean isAccessTokenValid(String token, UserDetails user) {
        Claims claims = extractClaims(token);
        return "access".equals(claims.get("type", String.class)) && user.getUsername().equals(claims.getSubject())
                && !claims.getExpiration().before(new Date());
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
