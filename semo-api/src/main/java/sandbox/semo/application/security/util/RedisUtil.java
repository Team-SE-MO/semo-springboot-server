package sandbox.semo.application.security.util;

import io.jsonwebtoken.Claims;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Log4j2
public class RedisUtil {

    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtUtil jwtUtil;

    private static final String BLACKLIST_PREFIX = "blacklist:";

    public void addToBlacklist(String token) {
        try {
            Claims claims = jwtUtil.validateAndGetClaimsFromToken(token);
            long remainingTime = claims.getExpiration().getTime() - System.currentTimeMillis();

            if (remainingTime > 0) {
                String key = BLACKLIST_PREFIX + token;
                redisTemplate.opsForValue().set(
                    key,
                    "logout",
                    remainingTime,
                    TimeUnit.MILLISECONDS
                );
                log.info(">>> [ ✅ 토큰 블랙리스트 추가 완료. 만료까지 남은 시간: {}ms ]", remainingTime);
            } else {
                log.info(">>> [ ℹ️ 이미 만료된 토큰입니다 ]");
            }
        } catch (RedisConnectionFailureException e) {
            log.error(">>> [ ❌ Redis 연결 실패: {} ]", e.getMessage());
            throw new RuntimeException("Redis 연결에 실패했습니다", e);
        } catch (Exception e) {
            log.error(">>> [ ❌ Redis 저장 실패: {} ]", e.getMessage());
            throw new RuntimeException("Redis 저장 실패", e);
        }
    }

    public boolean isBlacklisted(String token) {
        try {
            String key = BLACKLIST_PREFIX + token;
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error(">>> [ ❌ Redis 조회 실패: {} ]", e.getMessage());
            return false;
        }
    }
}

