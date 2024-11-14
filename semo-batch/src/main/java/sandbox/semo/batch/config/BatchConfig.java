package sandbox.semo.batch.config;

import com.amazonaws.services.s3.AmazonS3;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
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
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.OraclePagingQueryProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import sandbox.semo.batch.service.step.CompanyPartitionedFileWriter;
import sandbox.semo.batch.service.step.DailyTimeRangePartitioner;
import sandbox.semo.batch.service.step.DeviceProcessor;
import sandbox.semo.batch.service.step.DeviceReaderListener;
import sandbox.semo.batch.service.step.DeviceWriter;
import sandbox.semo.batch.service.tasklet.DeleteMetaDataTasklet;
import sandbox.semo.batch.service.tasklet.DeleteTasklet;
import sandbox.semo.batch.service.tasklet.S3UploadTasklet;
import sandbox.semo.domain.common.config.QueryLoader;
import sandbox.semo.domain.common.crypto.AES256;
import sandbox.semo.domain.device.entity.Device;
import sandbox.semo.domain.monitoring.dto.request.CsvFileData;
import sandbox.semo.domain.monitoring.dto.request.DeviceCollectionInfo;
import sandbox.semo.domain.monitoring.repository.MonitoringRepository;
import sandbox.semo.domain.monitoring.repository.mapper.CsvFileDataRowMapper;

@Log4j2
@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    @Value("${backup.path}")
    private String backupBasePath;

    private static final int CHUNK_AND_PAGE_SIZE = 5;
    private static final int CHUNK_SIZE = 10000;
    private static final int GRID_SIZE = 6;

    private final MonitoringRepository monitoringRepository;
    private final AES256 aes256;
    private final EntityManagerFactory entityManagerFactory;
    private final DeviceReaderListener deviceReaderListener;
    private final JdbcTemplate jdbcTemplate;
    private final HikariDataSource dataSource;
    private final QueryLoader queryLoader;
    private final AmazonS3 amazonS3;

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

    @Bean(name = "collectSessionDataJob")
    public Job collectSessionDataJob(JobRepository jobRepository,
        PlatformTransactionManager transactionManager) {
        return new JobBuilder("collectSessionDataJob", jobRepository)
            .start(deviceCollectionStep(
                jobRepository, transactionManager,
                deviceReader(), deviceProcessor(), deviceWriter()))
            .build();
    }

    // ===== Store CSV File Job =====
    @Bean
    @StepScope
    public TaskExecutor storeCsvFileTaskExecutor() {
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
    public CompanyPartitionedFileWriter companyPartitionedFileWriter(
        @Value("#{jobParameters['saveDate']}") String saveDateStr) {
        return new CompanyPartitionedFileWriter(backupBasePath, saveDateStr);
    }

    @Bean
    @StepScope
    public Partitioner dailyTimeRangePartitioner(
        @Value("#{jobParameters['saveDate']}") String saveDateStr) {
        return new DailyTimeRangePartitioner(saveDateStr);
    }


    @Bean
    public Step storeCsvFileSlaveStep(JobRepository jobRepository,
        PlatformTransactionManager transactionManager) {
        return new StepBuilder("backupSlaveStep", jobRepository)
            .<CsvFileData, CsvFileData>chunk(CHUNK_SIZE, transactionManager)
            .reader(storeCsvFileReader(null, null))
            .writer(companyPartitionedFileWriter(null))
            .build();
    }

    @Bean
    @StepScope
    public JdbcPagingItemReader<CsvFileData> storeCsvFileReader(
        @Value("#{stepExecutionContext['startTime']}") String startTime,
        @Value("#{stepExecutionContext['endTime']}") String endTime) {

        log.info(">>> [ 🔍 파티션 조회 시작 - {} ~ {} ]", startTime, endTime);

        OraclePagingQueryProvider queryProvider = new OraclePagingQueryProvider();
        queryProvider.setSelectClause(queryLoader.getQuery("selectSessionDataForBackup"));
        queryProvider.setFromClause(queryLoader.getQuery("fromSessionDataForBackup"));
        queryProvider.setWhereClause(queryLoader.getQuery("whereSessionDataForBackup"));

        Map<String, Order> sortKeys = new LinkedHashMap<>();
        sortKeys.put("COLLECTED_AT", Order.ASCENDING);
        sortKeys.put("SID", Order.ASCENDING);
        sortKeys.put("DEVICE_ID", Order.ASCENDING);
        queryProvider.setSortKeys(sortKeys);

        return new JdbcPagingItemReaderBuilder<CsvFileData>()
            .name("retentionReader_" + startTime)
            .dataSource(dataSource)
            .queryProvider(queryProvider)
            .parameterValues(Map.of(
                "startTime", LocalDateTime.parse(startTime),
                "endTime", LocalDateTime.parse(endTime)
            ))
            .pageSize(CHUNK_SIZE)
            .fetchSize(CHUNK_SIZE) // 매우중요..
            .saveState(false)
            .rowMapper(new CsvFileDataRowMapper())
            .build();
    }

    @Bean
    public Step storeCsvFileStep(JobRepository jobRepository,
        PlatformTransactionManager transactionManager) {
        return new StepBuilder("backupMasterStep", jobRepository)
            .partitioner("backupSlaveStep", dailyTimeRangePartitioner(null))
            .step(storeCsvFileSlaveStep(jobRepository, transactionManager))
            .gridSize(GRID_SIZE)
            .taskExecutor(storeCsvFileTaskExecutor())
            .build();
    }

    @Bean
    @StepScope
    public S3UploadTasklet s3UploadTasklet() {
        return new S3UploadTasklet(amazonS3);
    }

    @Bean
    public Step s3UploadStep(
        JobRepository jobRepository,
        PlatformTransactionManager transactionManager) {
        return new StepBuilder("s3UploadStep", jobRepository)
            .tasklet(s3UploadTasklet(), transactionManager)
            .build();
    }

    @Bean(name = "storeCsvFileJob")
    public Job storeCsvFileJob(JobRepository jobRepository,
        PlatformTransactionManager transactionManager) {
        return new JobBuilder("storeCsvFileJob", jobRepository)
            .start(storeCsvFileStep(jobRepository, transactionManager))
            .next(s3UploadStep(jobRepository, transactionManager))
            .build();
    }

    // ===== Retention Job =====

    @Bean
    @StepScope
    public DeleteTasklet deleteTasklet() {
        return new DeleteTasklet(monitoringRepository);
    }

    @Bean
    public Step deleteStep(
        JobRepository jobRepository,
        PlatformTransactionManager transactionManager) {
        return new StepBuilder("deleteStep", jobRepository)
            .tasklet(deleteTasklet(), transactionManager)
            .build();
    }

    @Bean
    @StepScope
    public DeleteMetaDataTasklet metaDataDeleteTasklet() {
        return new DeleteMetaDataTasklet(jdbcTemplate, monitoringRepository);
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
            .start(deleteStep(jobRepository, transactionManager))
            .next(metaDataDeleteStep(jobRepository, transactionManager))
            .build();
    }
}
