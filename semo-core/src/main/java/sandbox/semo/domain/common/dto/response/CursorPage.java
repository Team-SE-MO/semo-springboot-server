package sandbox.semo.domain.common.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CursorPage<T> {

    private List<T> content;            // 현재 페이지 데이터
    private LocalDateTime nextCursor;   // 다음 데이터 요청 커서

}
