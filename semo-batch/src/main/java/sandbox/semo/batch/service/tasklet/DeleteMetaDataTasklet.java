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
        LocalDateTime retentionDate = LocalDateTime.parse(retentionDateStr);
        log.info(">>> [ 🗑 배치 메타데이터 삭제 시작 - 기준일: {} ]", retentionDate);

        monitoringRepository.deleteJobExecutionParamsDataList(retentionDate);

        // BATCH_JOB_EXECUTION_CONTEXT 삭제
        jdbcTemplate.update(
            "DELETE FROM BATCH_JOB_EXECUTION_CONTEXT WHERE JOB_EXECUTION_ID IN " +
                "(SELECT JOB_EXECUTION_ID FROM BATCH_JOB_EXECUTION WHERE CREATE_TIME < ?)",
            retentionDate
        );

        // BATCH_STEP_EXECUTION_CONTEXT 삭제
        jdbcTemplate.update(
            "DELETE FROM BATCH_STEP_EXECUTION_CONTEXT WHERE STEP_EXECUTION_ID IN " +
                "(SELECT STEP_EXECUTION_ID FROM BATCH_STEP_EXECUTION WHERE START_TIME < ?)",
            retentionDate
        );

        // BATCH_STEP_EXECUTION 삭제
        jdbcTemplate.update(
            "DELETE FROM BATCH_STEP_EXECUTION WHERE JOB_EXECUTION_ID IN " +
                "(SELECT JOB_EXECUTION_ID FROM BATCH_JOB_EXECUTION WHERE CREATE_TIME < ?)",
            retentionDate
        );

        // BATCH_JOB_EXECUTION 삭제
        jdbcTemplate.update(
            "DELETE FROM BATCH_JOB_EXECUTION WHERE CREATE_TIME < ?",
            retentionDate
        );

        // BATCH_JOB_INSTANCE 삭제
        jdbcTemplate.update(
            "DELETE FROM BATCH_JOB_INSTANCE WHERE JOB_INSTANCE_ID NOT IN " +
                "(SELECT JOB_INSTANCE_ID FROM BATCH_JOB_EXECUTION)"
        );

        log.info(">>> [ ✅ 배치 메타데이터 삭제 완료 ]");
        return RepeatStatus.FINISHED;
    }
}

