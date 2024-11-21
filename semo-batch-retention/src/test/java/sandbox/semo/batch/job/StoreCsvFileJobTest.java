package sandbox.semo.batch.job;

import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
public class StoreCsvFileJobTest {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier("storeCsvFileJob")
    private Job storeCsvFileJob;

    @Test
    public void testStoreCsvFileJobSingleThread() throws Exception {
        // 싱글스레드로 Job 실행
        JobParameters jobParameters = new JobParametersBuilder()
            .addString("saveDate", LocalDateTime.now().minusDays(1).toString())
            .addLong("timestamp", System.currentTimeMillis())
            .toJobParameters();

        long startTime = System.currentTimeMillis();
        jobLauncher.run(storeCsvFileJob, jobParameters);
        long endTime = System.currentTimeMillis();

        log.info(">>> [ 싱글스레드 실행 시간: {} ms ]", (endTime - startTime));
    }

    @Test
    public void testStoreCsvFileJobMultiThread() throws Exception {
        // 멀티스레드로 Job 실행
        JobParameters jobParameters = new JobParametersBuilder()
            .addString("saveDate", LocalDateTime.now().minusDays(1).toString())
            .addLong("timestamp", System.currentTimeMillis())
            .toJobParameters();

        long startTime = System.currentTimeMillis();
        jobLauncher.run(storeCsvFileJob, jobParameters);
        long endTime = System.currentTimeMillis();

        log.info(">>> [ 멀티스레드 실행 시간: {} ms ]", (endTime - startTime));
    }
}