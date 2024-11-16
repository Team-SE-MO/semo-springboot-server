package sandbox.semo.batch.service.step;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

@Log4j2
@RequiredArgsConstructor
public class DailyTimeRangePartitioner implements Partitioner {

    private final String saveDateStr;

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        Map<String, ExecutionContext> result = new HashMap<>();
        LocalDateTime saveDate = LocalDateTime.parse(saveDateStr);

        LocalDateTime startOfDay = saveDate
            .truncatedTo(ChronoUnit.DAYS);

        for (int i = 0; i < gridSize; i++) {
            ExecutionContext context = new ExecutionContext();

            LocalDateTime partitionStart = startOfDay.plusHours(i * 4);
            LocalDateTime partitionEnd = i == gridSize - 1
                ? startOfDay.plusDays(1).minusNanos(1)
                : startOfDay.plusHours((i + 1) * 4);

            context.putString("startTime", partitionStart.toString());
            context.putString("endTime", partitionEnd.toString());

            result.put("partition" + i, context);
            log.info(">>> [ 🕒 파티션 생성 - partition{}: {} ~ {} ]",
                i,
                partitionStart,
                partitionEnd);
        }

        return result;
    }
}
