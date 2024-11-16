package sandbox.semo.domain.common.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CursorPage<T> {

    private long pageButton;
    private List<T> content;    // 현재 페이지 데이터
    private boolean hasNext;    // 다음 페이지 여부
    private Long nextCursor;    // 다음 페이지 커서
}
