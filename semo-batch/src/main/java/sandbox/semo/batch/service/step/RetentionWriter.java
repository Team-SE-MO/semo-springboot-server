package sandbox.semo.batch.service.step;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import sandbox.semo.domain.monitoring.entity.SessionData;
import sandbox.semo.domain.monitoring.repository.MonitoringRepository;

@Log4j2
@StepScope
@RequiredArgsConstructor
public class RetentionWriter implements ItemWriter<SessionData>, StepExecutionListener {

    private final MonitoringRepository monitoringRepository;

       @Value("#{jobParameters['retentionDate']}")
    private String retentionDateStr;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info(">>> [ 🚀 Retention Writer 초기화 ]");
    }

    @Override
    public void write(Chunk<? extends SessionData> chunk) {
        LocalDateTime retentionDate = LocalDateTime.parse(retentionDateStr);
        List<SessionData> items = new ArrayList<>(chunk.getItems());

         if (items.isEmpty()) {
        log.warn(">>> [ ⚠️ Writer로 전달된 데이터가 없습니다 ]");
        return;
    }

        try {
            monitoringRepository.deleteExpiredSessionDataList(items,retentionDate);
            log.info(">>> [ 🗑️ {} 개의 만료된 세션 데이터 삭제 완료 - 기준일: {}, Thread: {} ]",
                items.size(), retentionDate, Thread.currentThread().getName());
        } catch (Exception e) {
            log.error(">>> [ ❌ 세션 데이터 삭제 중 오류 발생: {} ]", e.getMessage());
            throw e;
        }
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
         log.info(">>> [ ✅ 데이터 삭제 작업 완료 - 처리 건수: {}, 소요 시간: {}ms ]",
            stepExecution.getWriteCount(),
            java.time.Duration.between(stepExecution.getStartTime(), stepExecution.getEndTime()).toMillis());
        return ExitStatus.COMPLETED;
    }
}