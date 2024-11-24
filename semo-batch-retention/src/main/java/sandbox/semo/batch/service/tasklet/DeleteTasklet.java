package sandbox.semo.batch.service.tasklet;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import sandbox.semo.domain.monitoring.repository.MonitoringRepository;

@Log4j2
@StepScope
@RequiredArgsConstructor
public class DeleteTasklet implements Tasklet {

    private final MonitoringRepository monitoringRepository;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {

        try {
            monitoringRepository.truncateSessionDataList();
            log.info(">>> [ 🗑️ 만료된 세션 데이터 삭제 완료 ]");
            return RepeatStatus.FINISHED;
        } catch (Exception e) {
            log.error(">>> [ ❌ 세션 데이터 삭제 중 오류 발생: {} ]", e.getMessage());
            throw e;
        }
    }
}
