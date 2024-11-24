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
    @DisplayName("단일스레드와 멀티스레드 배치 작업 속도 비교")
    void compareSingleAndMultiThreadTest() throws Exception {
        // 단일스레드 테스트
        Step singleThreadStep = createStep(false, 100); // 청크 사이즈 100
        Job singleThreadJob = createJob(singleThreadStep);

        log.info("=== 단일스레드 테스트 시작 ===");
        long singleThreadExecutionTime = executeJob(singleThreadJob);
        log.info("단일스레드 실행 시간: {} ms", singleThreadExecutionTime);

        // 멀티스레드 테스트
        Step multiThreadStep = createStep(true, 5); // 청크 사이즈 5
        Job multiThreadJob = createJob(multiThreadStep);

        log.info("=== 멀티스레드 테스트 시작 ===");
        long multiThreadExecutionTime = executeJob(multiThreadJob);
        log.info("멀티스레드 실행 시간: {} ms", multiThreadExecutionTime);

        // 속도 차이
        log.info("=== 속도 차이 ===");
        log.info("단일스레드 실행 시간: {} ms", singleThreadExecutionTime);
        log.info("멀티스레드 실행 시간: {} ms", multiThreadExecutionTime);

        // 속도 향상률 계산
        double performanceImprovement =
            ((double) (singleThreadExecutionTime - multiThreadExecutionTime) / singleThreadExecutionTime) * 100;
        log.info("성능 향상률: {}", String.format("%.2f%%", performanceImprovement));
    }

    private Step createStep(boolean useMultiThread, int chunkSize) {
        SimpleStepBuilder<Device, DeviceCollectionInfo> stepBuilder = new StepBuilder(
            "deviceCollectionStep", jobRepository)
            .<Device, DeviceCollectionInfo>chunk(chunkSize, transactionManager) // 청크 사이즈 인자로 설정
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