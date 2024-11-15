package sandbox.semo.batch.service.tasklet;


import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import sandbox.semo.domain.monitoring.repository.MonitoringRepository;

@Log4j2
@RequiredArgsConstructor
public class DeleteMetaDataTasklet implements Tasklet {

    private final JdbcTemplate jdbcTemplate;
    private final MonitoringRepository monitoringRepository;

    @Value("#{jobParameters['retentionDate']}")
    private String retentionDateStr;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
        throws Exception {
        LocalDateTime retentionDate = LocalDateTime.parse(retentionDateStr).minusDays(5);
        log.info(">>> [ 🗑 배치 메타데이터 삭제 시작 - 기준일: {} ]", retentionDate);

        monitoringRepository.deleteStepExecutionContext(retentionDate);
        monitoringRepository.deleteStepExecution(retentionDate);
        monitoringRepository.deleteJobExecutionContextDataList(retentionDate);
        monitoringRepository.deleteJobExecutionParamsDataList(retentionDate);
        monitoringRepository.deleteJobExecution(retentionDate);
        monitoringRepository.deleteJobInstance(retentionDate);

        log.info(">>> [ ✅ 배치 메타데이터 삭제 완료 ]");
        return RepeatStatus.FINISHED;
    }
}

