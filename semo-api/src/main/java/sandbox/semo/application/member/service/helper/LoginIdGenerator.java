package sandbox.semo.application.member.service.helper;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class LoginIdGenerator {

    @PersistenceContext
    private EntityManager entityManager;

    public String generateLoginId(String role, String taxId) {
        String prefix = role.equals("ROLE_ADMIN") ? "A" : "U";
        String splitTaxId = taxId.replace("-", "");

        BigDecimal sequenceValue = (BigDecimal) entityManager.createNativeQuery(
                        "SELECT GENERATE_LOGIN_ID_SEQ.NEXTVAL FROM DUAL")
                .getSingleResult();
        Long currentSequenceValue = sequenceValue.longValue();

        String formattedNum = String.format("%d", currentSequenceValue);

        return prefix + splitTaxId + formattedNum;

    }
}