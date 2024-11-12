package sandbox.semo.batch.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
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
import org.springframework.batch.item.ExecutionContext;
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
import sandbox.semo.batch.service.step.DeviceProcessor;
import sandbox.semo.batch.service.step.DeviceReaderListener;
import sandbox.semo.batch.service.step.DeviceWriter;
import sandbox.semo.batch.service.step.RetentionFileWriter;
import sandbox.semo.batch.service.tasklet.DeleteMetaDataTasklet;
import sandbox.semo.batch.service.tasklet.DeleteTasklet;
import sandbox.semo.domain.common.crypto.AES256;
import sandbox.semo.domain.device.entity.Device;
import sandbox.semo.domain.monitoring.dto.request.CsvFileData;
import sandbox.semo.domain.monitoring.dto.request.DeviceCollectionInfo;
import sandbox.semo.domain.monitoring.repository.MonitoringRepository;

@Log4j2
@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    @Value("${backup.path}")
    private String backupBasePath;

    private static final int CHUNK_AND_PAGE_SIZE = 5;
    private static final int CHUNK_SIZE = 10000;

    private final MonitoringRepository monitoringRepository;
    private final AES256 aes256;
    private final EntityManagerFactory entityManagerFactory;
    private final DeviceReaderListener deviceReaderListener;
    private final JdbcTemplate jdbcTemplate;
    private final HikariDataSource dataSource;

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

    // ===== Store CSV File Job =====
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

//     @Bean
//     @StepScope
//     public JpaPagingItemReader<SessionData> retentionFileReader(
//         @Value("#{jobParameters['saveDate']}") String saveDateStr) {
//         LocalDateTime saveDate = LocalDateTime.parse(saveDateStr);
//         log.info(">>> [ 🔍 보관 대상 데이터 조회 시작 - 기준일: {} ]", saveDate);
//          long totalCount = entityManagerFactory.createEntityManager()
//             .createQuery("SELECT COUNT(s) FROM SessionData s WHERE s.id.collectedAt < :saveDate", Long.class)
//             .setParameter("saveDate", saveDate)
//             .getSingleResult();
//
//         log.info(">>> [ 📊 전체 대상 데이터 건수: {} ]", totalCount);
//
//         return new JpaPagingItemReaderBuilder<SessionData>()
//             .name("retentionReader")
//             .entityManagerFactory(entityManagerFactory)
//             .pageSize(CHUNK_SIZE)
//            .queryString("SELECT s FROM SessionData s " +
//                "WHERE s.id.collectedAt < :saveDate ")
//             .parameterValues(Map.of("saveDate", saveDate))
//             .saveState(false)
//             .build();
//     }

    private String createBackupPath() {
        LocalDateTime now = LocalDateTime.now();
        return String.format("%s/%d/%02d/session_data_%s.csv",
            backupBasePath,
            now.getYear(),
            now.getMonthValue(),
            now.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
    }

    @Bean
    @StepScope
    public RetentionFileWriter retentionFileWriter() {
        String backupPath = createBackupPath();
        return new RetentionFileWriter(backupPath);
    }

    @Bean
    @StepScope
    public CompanyPartitionedFileWriter companyPartitionedFileWriter() {
        return new CompanyPartitionedFileWriter(backupBasePath);
    }

    @Bean
    public Step backupStep(JobRepository jobRepository,
        PlatformTransactionManager transactionManager) {
        return new StepBuilder("backupMasterStep", jobRepository)
            .partitioner("backupSlaveStep", dailyTimeRangePartitioner(null))
            .step(backupSlaveStep(jobRepository, transactionManager))
            .gridSize(6)
            .taskExecutor(retentionTaskExecutor())
            .build();
    }

    @Bean
    @StepScope
    public Partitioner dailyTimeRangePartitioner(
        @Value("#{jobParameters['saveDate']}") String saveDateStr) {
        return gridSize -> {
            Map<String, ExecutionContext> result = new HashMap<>();
            LocalDateTime saveDate = LocalDateTime.parse(saveDateStr);

            // 전일 00:00 ~ 24:00 계산
            LocalDateTime startOfDay = saveDate.minusDays(1).truncatedTo(ChronoUnit.DAYS);
            LocalDateTime endOfDay = startOfDay.plusDays(1);

            // 4시간 단위로 분할
            int hoursPerPartition = 4;

            for (int i = 0; i < gridSize; i++) {
                ExecutionContext context = new ExecutionContext();

                LocalDateTime partitionStart = startOfDay.plusHours(i * hoursPerPartition);
                LocalDateTime partitionEnd = i == gridSize - 1
                    ? endOfDay
                    : startOfDay.plusHours((i + 1) * hoursPerPartition);

                context.putString("startTime", partitionStart.toString());
                context.putString("endTime", partitionEnd.toString());

                result.put("partition" + i, context);
                log.info(">>> [ 🕒 파티션 생성 - partition{}: {} ~ {} ]",
                    i, partitionStart.format(DateTimeFormatter.ISO_LOCAL_TIME),
                    partitionEnd.format(DateTimeFormatter.ISO_LOCAL_TIME));
            }

            return result;
        };
    }

    @Bean
    public Step backupSlaveStep(JobRepository jobRepository,
        PlatformTransactionManager transactionManager) {
        return new StepBuilder("backupSlaveStep", jobRepository)
            .<CsvFileData, CsvFileData>chunk(CHUNK_SIZE, transactionManager)
            .reader(retentionFileReader(null, null))
            .writer(companyPartitionedFileWriter())
            .build();
    }

    @Bean
    @StepScope
    public JdbcPagingItemReader<CsvFileData> retentionFileReader(
        @Value("#{stepExecutionContext['startTime']}") String startTime,
        @Value("#{stepExecutionContext['endTime']}") String endTime) {

        log.info(">>> [ 🔍 파티션 조회 시작 - {} ~ {} ]", startTime, endTime);

        OraclePagingQueryProvider queryProvider = new OraclePagingQueryProvider();
        queryProvider.setSelectClause("""
                /*+ PARALLEL(s 4) INDEX(s IDX_SESSION_DATA_COLLECTED) */
                s.COLLECTED_AT as COLLECTED_AT,
                s.SID as SID,
                s.DEVICE_ID as DEVICE_ID,
                s.SERIAL as SERIAL,
                s.USERNAME as USERNAME,
                s.COMMAND as COMMAND,
                s.COMMAND_NAME as COMMAND_NAME,
                s.STATUS as STATUS,
                s.SCHEMANAME as SCHEMA_NAME,
                s.OSUSER as OS_USER,
                s.PROCESS as PROCESS,
                s.MACHINE as MACHINE,
                s.PORT as PORT,
                s.TERMINAL as TERMINAL,
                s.PROGRAM as PROGRAM,
                s.TYPE as TYPE,
                s.SQL_ID as SQL_ID,
                s.SQL_EXEC_START as SQL_EXEC_START,
                s.SQL_EXEC_ID as SQL_EXEC_ID,
                s.SQL_TEXT as SQL_TEXT,
                s.MODULE as MODULE,
                s.ACTION as ACTION,
                s.LOGON_TIME as LOGON_TIME,
                s.LAST_CALL_ET as LAST_CALL_ET,
                s.FAILED_OVER as FAILED_OVER,
                s.BLOCKING_SESSION_STATUS as BLOCKING_SESSION_STATUS,
                s.EVENT as EVENT,
                s.WAIT_CLASS as WAIT_CLASS,
                s.STATE as STATE,
                s.WAIT_TIME_MICRO as WAIT_TIME_MICRO,
                s.TIME_REMAINING_MICRO as TIME_REMAINING_MICRO,
                s.SERVICE_NAME as SERVICE_NAME,
                d.COMPANY_ID as COMPANY_ID
            """);

        queryProvider.setFromClause(
            "SESSION_DATA s INNER JOIN DEVICES d ON d.DEVICE_ID = s.DEVICE_ID");
        queryProvider.setWhereClause(
            "s.COLLECTED_AT >= :startTime AND s.COLLECTED_AT < :endTime");

        Map<String, Order> sortKeys = new LinkedHashMap<>();
        sortKeys.put("COLLECTED_AT", Order.ASCENDING);
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
            .rowMapper((rs, rowNum) -> {
                CsvFileData data = new CsvFileData();
                data.setCollectedAt(rs.getTimestamp("COLLECTED_AT").toLocalDateTime());
                data.setSid(rs.getLong("SID"));
                data.setDeviceId(rs.getLong("DEVICE_ID"));
                data.setSerial(rs.getLong("SERIAL"));
                data.setUsername(rs.getString("USERNAME"));
                data.setCommand(rs.getLong("COMMAND"));
                data.setCommandName(rs.getString("COMMAND_NAME"));
                data.setStatus(rs.getString("STATUS"));
                data.setSchemaName(rs.getString("SCHEMA_NAME"));
                data.setOsUser(rs.getString("OS_USER"));
                data.setProcess(rs.getString("PROCESS"));
                data.setMachine(rs.getString("MACHINE"));
                data.setPort(rs.getInt("PORT"));
                data.setTerminal(rs.getString("TERMINAL"));
                data.setProgram(rs.getString("PROGRAM"));
                data.setType(rs.getString("TYPE"));
                data.setSqlId(rs.getString("SQL_ID"));

                Timestamp sqlExecStart = rs.getTimestamp("SQL_EXEC_START");
                if (sqlExecStart != null) {
                    data.setSqlExecStart(sqlExecStart.toLocalDateTime());
                }

                data.setSqlExecId(rs.getLong("SQL_EXEC_ID"));
                data.setSqlText(rs.getString("SQL_TEXT"));
                data.setModule(rs.getString("MODULE"));
                data.setAction(rs.getString("ACTION"));

                Timestamp logonTime = rs.getTimestamp("LOGON_TIME");
                if (logonTime != null) {
                    data.setLogonTime(logonTime.toLocalDateTime());
                }

                data.setLastCallEt(rs.getLong("LAST_CALL_ET"));
                data.setFailedOver(rs.getString("FAILED_OVER"));
                data.setBlockingSessionStatus(rs.getString("BLOCKING_SESSION_STATUS"));
                data.setEvent(rs.getString("EVENT"));
                data.setWaitClass(rs.getString("WAIT_CLASS"));
                data.setState(rs.getString("STATE"));
                data.setWaitTimeMicro(rs.getLong("WAIT_TIME_MICRO"));
                data.setTimeRemainingMicro(rs.getLong("TIME_REMAINING_MICRO"));
                data.setServiceName(rs.getString("SERVICE_NAME"));
                data.setCompanyId(rs.getLong("COMPANY_ID"));
                return data;
            })
            .build();
    }

    @Bean(name = "storeCsvFileJob")
    public Job backupOnlyJob(JobRepository jobRepository,
        PlatformTransactionManager transactionManager) {
        return new JobBuilder("storeCsvFileJob", jobRepository)
            .start(backupStep(jobRepository, transactionManager))
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
