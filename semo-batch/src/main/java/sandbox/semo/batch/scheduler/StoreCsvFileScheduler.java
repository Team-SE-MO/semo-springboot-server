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
public class StoreCsvFileScheduler {

    private final JobLauncher jobLauncher;
    @Qualifier("storeCsvFileJob")
    private final Job storeCsvFileJob;

    @Scheduled(cron = "0 07 02 * * *")  // 예: 매일 23시 30분에 실행
    public void runBackup() throws Exception {
        LocalDateTime saveDate = LocalDateTime.now()
            .minusDays(1)
            .withHour(22)
            .withMinute(45)
            .withSecond(0)
            .withNano(0);

        log.info(">>> [ 💾 데이터 저장 작업 시작 - 기준일: {} ]", saveDate);
        jobLauncher.run(
            storeCsvFileJob,
            new JobParametersBuilder()
                .addString("saveDate", saveDate.toString())
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters()
        );
    }
}