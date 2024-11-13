package sandbox.semo.application.common.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommonServiceImpl implements CommonService {

    private final EntityManager entityManager;

    @Override
    public LocalDateTime fetchDatabaseServerTime() {
        Query query = entityManager.createNativeQuery("SELECT TO_CHAR(CURRENT_TIMESTAMP, 'YYYY-MM-DD\"T\"HH24:MI:SS') FROM DUAL");
        String dbTime = (String) query.getSingleResult();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        return LocalDateTime.parse(dbTime, formatter);
    }

}
