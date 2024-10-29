package sandbox.semo.application.security.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String username,String role,Long companyId) {
        SecretKey key = getSigningKey();
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .claim("company_id",companyId)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusMillis(expiration)))
                .signWith(key)
                .compact();
    }

    public Boolean validateToken(String token, String username) {
        final String extractedUsername = getUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    public String getUsername(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    public Date getExpirationDate(String token) {
        return getClaimsFromToken(token).getExpiration();
    }

    public String getRole(String token) {
        return getClaimsFromToken(token).get("role", String.class);
    }

    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDate(token);
        return expiration.before(new Date());
    }
}
