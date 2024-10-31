package sandbox.semo.application.security.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Long memberId, String username, String role, Long companyId) {
        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .claim("role", role)
                .claim("login_id", username)
                .claim("company_id", companyId)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusMillis(expiration)))
                .signWith(getSigningKey())
                .compact();
    }

    public Date getExpirationDate(Claims claims) {
        return claims.getExpiration();
    }

    public Long getMemberId(Claims claims) {
        String subject = claims.getSubject();
        return Long.parseLong(subject);
    }

    public String getRole(Claims claims) {
        return claims.get("role", String.class);
    }

    public Long getCompanyId(Claims claims) {
        return claims.get("company_id", Long.class);
    }

    public String getLoginId(Claims claims) {
        return claims.get("login_id", String.class);
    }

    public Claims validateAndGetClaimsFromToken(String token) throws JwtException, ExpiredJwtException {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
