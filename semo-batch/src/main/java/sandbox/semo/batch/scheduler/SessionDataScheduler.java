package sandbox.semo.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Log4j2
@Configuration
@RequiredArgsConstructor
public class SessionDataScheduler {

    private final JobLauncher jobLauncher;
    private final Job job;

    @Scheduled(cron = "0/5 * * * * *")
    public void runCollector() throws Exception {
        log.info(">>> [ 📑 Run Collect Session Data ... ]");
        jobLauncher.run(job, new JobParametersBuilder()
                .addLong("runTime", System.currentTimeMillis())
                .toJobParameters());
    }

}
