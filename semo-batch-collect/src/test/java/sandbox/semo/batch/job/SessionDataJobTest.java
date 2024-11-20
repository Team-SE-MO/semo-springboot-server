package sandbox.semo.batch.job;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ItemReadListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.SimpleStepBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import sandbox.semo.batch.service.step.DeviceReaderListener;
import sandbox.semo.domain.device.entity.Device;
import sandbox.semo.domain.monitoring.dto.request.DeviceCollectionInfo;

@SpringBatchTest
@SpringBootTest
@Slf4j
class SessionDataJobTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private JpaPagingItemReader<Device> deviceReader;

    @Autowired
    private ItemProcessor<Device, DeviceCollectionInfo> deviceProcessor;

    @Autowired
    private ItemWriter<DeviceCollectionInfo> deviceWriter;

    @Autowired
    private DeviceReaderListener deviceReaderListener;

    @Autowired
    @Qualifier("deviceTaskExecutor")
    private TaskExecutor taskExecutor;

    @Test
    @DisplayName("단일스레드와 멀티스레드 성능을 비교한다")
    void compareThreadPerformanceTest() throws Exception {
        // 단일스레드 실행
        Step singleThreadStep = createStep(false);
        Job singleThreadJob = createJob(singleThreadStep);

        log.info("=== 단일스레드 테스트 시작 ===");
        long singleThreadTime = executeJob(singleThreadJob);
        log.info("단일스레드 실행 시간: {} ms", singleThreadTime);

        // 실행 사이 간격
        Thread.sleep(2000);

        // 멀티스레드 실행
        Step multiThreadStep = createStep(true);
        Job multiThreadJob = createJob(multiThreadStep);

        log.info("=== 멀티스레드 테스트 시작 ===");
        long multiThreadTime = executeJob(multiThreadJob);
        log.info("멀티스레드 실행 시간: {} ms", multiThreadTime);

        double performanceImprovement =
            ((double) (singleThreadTime - multiThreadTime) / singleThreadTime) * 100;
        log.info("성능 향상률: {}", String.format("%.2f%%", performanceImprovement));
    }

    private Step createStep(boolean useMultiThread) {
        SimpleStepBuilder<Device, DeviceCollectionInfo> stepBuilder = new StepBuilder(
            "deviceCollectionStep", jobRepository)
            .<Device, DeviceCollectionInfo>chunk(100, transactionManager)
            .reader(deviceReader)
            .processor(deviceProcessor)
            .writer(deviceWriter)
            .listener((ItemReadListener<Device>) deviceReaderListener);

        if (useMultiThread) {
            stepBuilder.taskExecutor(taskExecutor);
        }

        return stepBuilder.build();
    }

    private Job createJob(Step step) {
        return new JobBuilder("collectSessionDataJob", jobRepository)
            .start(step)
            .build();
    }

    private long executeJob(Job job) throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
            .addLong("time", System.currentTimeMillis())
            .addString("uuid", UUID.randomUUID().toString())
            .toJobParameters();

        long startTime = System.currentTimeMillis();
        JobExecution jobExecution = jobLauncherTestUtils.getJobLauncher().run(job, jobParameters);
        long executionTime = System.currentTimeMillis() - startTime;

        StepExecution stepExecution = jobExecution.getStepExecutions().iterator().next();
        log.info("처리된 아이템 수: {}", stepExecution.getReadCount());
        log.info("커밋 수: {}", stepExecution.getCommitCount());
        log.info("처리 상태: {}", stepExecution.getExitStatus());

        return executionTime;
    }
}