package sandbox.semo.application.common.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class LoginIdGeneratorUtil {

    private static final Map<String, AtomicInteger> lastNumberMap = new ConcurrentHashMap<>();

    public static String generateLoginId(String role, String taxId) {
        String prefix = role.equals("ADMIN") ? "A" : "U";

        String splitTaxId = taxId.replace("-", "");

        //taxId가 이미 존재하면 pass, 존재하지 않다면 taxId로 키와 초기값 생성해 Map에 넣어줌
        AtomicInteger lastNumber = lastNumberMap.computeIfAbsent(splitTaxId,
                k -> new AtomicInteger(1));

        String lastTwoDigits = String.format("%02d", lastNumber.getAndIncrement());

        return prefix + splitTaxId + lastTwoDigits;
    }
}