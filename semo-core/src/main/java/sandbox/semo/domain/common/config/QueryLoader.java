package sandbox.semo.domain.common.config;

import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.springframework.stereotype.Component;

@Component
public class QueryLoader {

    private final ResourceBundle queries;

    public QueryLoader() {
        try {
            queries = ResourceBundle.getBundle("sql/batch-queries");
        } catch (MissingResourceException e) {
            throw new RuntimeException("쿼리 파일을 불러오는 데 실패했습니다.", e);
        }
    }

    public String getQuery(String key) {
        if (queries.containsKey(key)) {
            return queries.getString(key);
        } else {
            throw new IllegalArgumentException("해당 키에 대한 쿼리가 없습니다: " + key);
        }
    }

}
