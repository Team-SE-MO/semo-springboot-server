package sandbox.semo.application.security.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
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

    private String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }


    private SecretKey getSigningKey(String salt) {
        String combinedSecret = secret + salt;
        return Keys.hmacShaKeyFor(combinedSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Long memberId, String username, String role, Long companyId) {
        String salt = generateSalt();
        return Jwts.builder()
            .subject(String.valueOf(memberId))
            .claim("role", role)
            .claim("loginId", username)
            .claim("companyId", companyId)
            .claim("salt", salt)
            .issuedAt(Date.from(Instant.now()))
            .expiration(Date.from(Instant.now().plusMillis(expiration)))
            .signWith(getSigningKey(salt), Jwts.SIG.HS256)
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
        return claims.get("companyId", Long.class);
    }

    public String getLoginId(Claims claims) {
        return claims.get("loginId", String.class);
    }

    private String extractSaltFromToken(String token) {
        try {
            // 1. 토큰을 점(.)으로 분리
            String[] chunks = token.split("\\.");
            if (chunks.length != 3) {
                log.error(">>> [ ❌ 잘못된 JWT 형식입니다 ]");
                throw new JwtException("");
            }

            // 2. payload(두 번째 부분)를 Base64 디코딩
            String payload = new String(Base64.getDecoder().decode(chunks[1]));

            // 3. salt 값 추출
            int startIndex = payload.indexOf("\"salt\":\"") + 8;
            int endIndex = payload.indexOf("\"", startIndex);

            if (startIndex < 8 || endIndex == -1) {  // salt가 없는 경우
                log.error(">>> [ ❌ salt값이 존재하지않습니다 ]");
                throw new JwtException("");
            }
            String salt = payload.substring(startIndex, endIndex);
            log.debug(">>> Extracted salt: {}", salt);
            return salt;
        } catch (Exception e) {
            log.error(">>> [ ❌ 토큰 파싱 실패 ]");
            throw new JwtException("");
        }
    }

    public Claims validateAndGetClaimsFromToken(String token)
        throws JwtException, ExpiredJwtException {
        String salt = extractSaltFromToken(token);
        return Jwts.parser()
            .verifyWith(getSigningKey(salt))
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
