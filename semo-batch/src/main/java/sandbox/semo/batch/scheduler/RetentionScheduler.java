package sandbox.semo.batch.scheduler;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class RetentionScheduler {

    private final JobLauncher jobLauncher;
    @Qualifier("retentionJob")
    private final Job retentionJob;

    @Scheduled(cron = "0 0 0 * * *")
    public void runRetention() throws Exception {
        LocalDateTime retentionDate = LocalDateTime.now();

        log.info(">>> [ 📑 데이터 보존 기간 관리 시작 - 기준일: {} ]", retentionDate);
        jobLauncher.run(
            retentionJob,
            new JobParametersBuilder()
                .addString("retentionDate", retentionDate.toString())
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters()
        );
    }
}
