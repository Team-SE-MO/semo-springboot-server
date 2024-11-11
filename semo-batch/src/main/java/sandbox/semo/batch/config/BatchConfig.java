package sandbox.semo.batch.config;

import jakarta.persistence.EntityManagerFactory;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.ItemReadListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import sandbox.semo.batch.service.step.DeviceProcessor;
import sandbox.semo.batch.service.step.DeviceReaderListener;
import sandbox.semo.batch.service.step.DeviceWriter;
import sandbox.semo.batch.service.step.RetentionFileWriter;
import sandbox.semo.batch.service.step.RetentionWriter;
import sandbox.semo.batch.service.tasklet.DeleteMetaDataTasklet;
import sandbox.semo.batch.service.tasklet.DeleteTasklet;
import sandbox.semo.domain.common.crypto.AES256;
import sandbox.semo.domain.device.entity.Device;
import sandbox.semo.domain.monitoring.dto.request.DeviceCollectionInfo;
import sandbox.semo.domain.monitoring.entity.SessionData;
import sandbox.semo.domain.monitoring.repository.MonitoringRepository;

@Log4j2
@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    @Value("${backup.path}")
    private String backupBasePath;

    private static final int CHUNK_AND_PAGE_SIZE = 5;
    private static final int CHUNK_SIZE = 1000;

    private final MonitoringRepository monitoringRepository;
    private final AES256 aes256;
    private final EntityManagerFactory entityManagerFactory;
    private final DeviceReaderListener deviceReaderListener;
    private final JdbcTemplate jdbcTemplate;

    // ===== Device Collection Job =====
    @Bean
    public TaskExecutor deviceTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(6);
        executor.setMaxPoolSize(6);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("device-task-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<Device> deviceReader() {
        return new JpaPagingItemReaderBuilder<Device>()
            .name("deviceReader")
            .entityManagerFactory(entityManagerFactory)
            .pageSize(CHUNK_AND_PAGE_SIZE)
            .queryString("SELECT d FROM Device d ORDER BY d.id")
            .saveState(false)
            .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<Device, DeviceCollectionInfo> deviceProcessor() {
        LocalDateTime collectedAt = LocalDateTime.now().withNano(0);
        log.info(">>> [ ⏰ Job 시작 시간 설정: {} ]", collectedAt);
        return new DeviceProcessor(aes256, monitoringRepository, collectedAt);
    }

    @Bean
    @StepScope
    public ItemWriter<DeviceCollectionInfo> deviceWriter() {
        return new DeviceWriter(monitoringRepository);
    }

    @Bean
    protected Step deviceCollectionStep(
        JobRepository jobRepository, PlatformTransactionManager transactionManager,
        ItemReader<Device> reader,
        ItemProcessor<Device, DeviceCollectionInfo> processor,
        ItemWriter<DeviceCollectionInfo> writer) {
        return new StepBuilder("deviceStatusValidStep", jobRepository)
            .<Device, DeviceCollectionInfo>chunk(CHUNK_AND_PAGE_SIZE, transactionManager)
            .reader(reader)
            .processor(processor)
            .writer(writer)
            .listener((StepExecutionListener) deviceReaderListener)
            .listener((ItemReadListener<? super Device>) deviceReaderListener) // Step 리스너
            .taskExecutor(deviceTaskExecutor())
            .build();
    }

    @Bean(name = "sessionDataJob")
    public Job sessionDataJob(JobRepository jobRepository,
        PlatformTransactionManager transactionManager) {
        return new JobBuilder("chunksJob", jobRepository)
            .start(deviceCollectionStep(
                jobRepository, transactionManager,
                deviceReader(), deviceProcessor(), deviceWriter()))
            .build();
    }

    // ===== Retention Job =====

    @Bean
    @StepScope
    public TaskExecutor retentionTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("retention-task-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<SessionData> retentionFileReader(
        @Value("#{jobParameters['retentionDate']}") String retentionDateStr) {
        LocalDateTime retentionDate = LocalDateTime.parse(retentionDateStr);
        log.info(">>> [ 🔍 삭제 대상 데이터 조회 시작 - 기준일: {} ]", retentionDate);

        return new JpaPagingItemReaderBuilder<SessionData>()
            .name("retentionReader")
            .entityManagerFactory(entityManagerFactory)
            .pageSize(CHUNK_SIZE)
            .queryString("SELECT s FROM SessionData s " +
                "WHERE s.id.collectedAt < :retentionDate ")
            .parameterValues(Map.of("retentionDate", retentionDate))
            .saveState(false)
            .build();
    }

    @Bean
    @StepScope
    public ItemWriter<SessionData> retentionWriter() {
        return new RetentionWriter(monitoringRepository);
    }

    @Bean
    @StepScope
    public RetentionFileWriter retentionFileWriter() {
        String backupPath = createBackupPath();
        return new RetentionFileWriter(backupPath);
    }

    private String createBackupPath() {
        LocalDateTime now = LocalDateTime.now();
        return String.format("%s/%d/%02d/session_data_%s.csv",
            backupBasePath,
            now.getYear(),
            now.getMonthValue(),
            now.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
    }

    // Step 1: 백업 Step
    @Bean
    public Step backupStep(
        JobRepository jobRepository,
        PlatformTransactionManager transactionManager) {
        return new StepBuilder("backupStep", jobRepository)
            .<SessionData, SessionData>chunk(CHUNK_SIZE, transactionManager)
            .reader(retentionFileReader(null))
            .writer(retentionFileWriter())
            .build();
    }

    @Bean
    @StepScope
    public DeleteTasklet deleteTasklet() {
        return new DeleteTasklet(monitoringRepository);
    }
    // Step 2: 삭제 Step
    @Bean
    public Step deleteStep(
        JobRepository jobRepository,
        PlatformTransactionManager transactionManager) {
        return new StepBuilder("deleteStep", jobRepository)
            .tasklet(deleteTasklet(), transactionManager)
            .build();
    }

    // Step 3: 삭제 Step - backupBasePath를 localdate로 변환해서 해당 일자 전의 배치 메타테이블 데이터 삭제

    @Bean
@StepScope
public DeleteMetaDataTasklet metaDataDeleteTasklet() {
    return new DeleteMetaDataTasklet(jdbcTemplate,monitoringRepository);
}

    @Bean
    public Step metaDataDeleteStep(
    JobRepository jobRepository,
    PlatformTransactionManager transactionManager) {
    return new StepBuilder("deleteMetaDataStep", jobRepository)
        .tasklet(metaDataDeleteTasklet(), transactionManager)
            .build();
    }


    // Job 설정 수정
    @Bean(name = "retentionJob")
    public Job retentionJob(JobRepository jobRepository,
        PlatformTransactionManager transactionManager) {
        return new JobBuilder("retentionJob", jobRepository)
            .start(backupStep(jobRepository, transactionManager))
            .next(deleteStep(jobRepository, transactionManager))
            .next(metaDataDeleteStep(jobRepository, transactionManager))
            .build();
    }
}
