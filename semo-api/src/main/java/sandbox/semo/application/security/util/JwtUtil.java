package sandbox.semo.application.security.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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

    public String generateToken(String username, String role, Long companyId) {
        SecretKey key = getSigningKey();
        return Jwts.builder()
            .subject(username)
            .claim("role", role)
//            .claim("username_id")
            .claim("company_id", companyId)
            .issuedAt(Date.from(Instant.now()))
            .expiration(Date.from(Instant.now().plusMillis(expiration)))
            .signWith(getSigningKey())
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

    public Long getCompanyId(String token) {
        return getClaimsFromToken(token).get("company_id", Long.class);
    }

    private Claims getClaimsFromToken(String token) {
        // return Jwts.parser()
        //         .verifyWith(getSigningKey())
        //         .build()
        //         .parseSignedClaims(token)
        //         .getPayload();
        try {
            return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        } catch (ExpiredJwtException e) {
            log.error(">>> [ ❌ 만료된 JWT 토큰입니다. ]");
            throw e;
        } catch (JwtException e) {
            log.error(">>> [ ❌ 유효하지 않은 JWT 토큰입니다. ]", e);
            throw e;
        }
    }

    public Boolean isTokenExpired(String token) {
             final Date expiration = getExpirationDate(token);
             return expiration.before(new Date());
         }
//        final Date expiration = getExpirationDate(token);
//        if (!expiration.before(new Date())) {
//            throw new ExpiredJwtExeption("expore")
//        }
//        return true;
//            return expiration.before(new Date());
    }
