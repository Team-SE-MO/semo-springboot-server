package sandbox.semo.batch.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import sandbox.semo.batch.util.QueryLoader;

@Repository
@RequiredArgsConstructor
public class JdbcRepository {

    private final QueryLoader queryLoader;
    private final JdbcTemplate jdbcTemplate;

    public void deviceStatusUpdate(boolean status, Long deviceId) {
        String query = queryLoader.getQuery("updateDeviceStatus");
        jdbcTemplate.update(query, status, deviceId);
    }

}
