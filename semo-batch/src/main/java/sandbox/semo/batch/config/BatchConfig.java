package sandbox.semo.batch.config;

import com.zaxxer.hikari.HikariDataSource;
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
import sandbox.semo.batch.service.step.CompanyPartitionedFileWriter;
import sandbox.semo.batch.service.step.DeviceProcessor;
import sandbox.semo.batch.service.step.DeviceReaderListener;
import sandbox.semo.batch.service.step.DeviceWriter;
import sandbox.semo.batch.service.step.RetentionFileWriter;
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

     @Bean
     @StepScope
     public JpaPagingItemReader<SessionData> retentionFileReader(
         @Value("#{jobParameters['saveDate']}") String saveDateStr) {
         LocalDateTime saveDate = LocalDateTime.parse(saveDateStr);
         log.info(">>> [ 🔍 보관 대상 데이터 조회 시작 - 기준일: {} ]", saveDate);
          long totalCount = entityManagerFactory.createEntityManager()
             .createQuery("SELECT COUNT(s) FROM SessionData s WHERE s.id.collectedAt < :saveDate", Long.class)
             .setParameter("saveDate", saveDate)
             .getSingleResult();

         log.info(">>> [ 📊 전체 대상 데이터 건수: {} ]", totalCount);

         return new JpaPagingItemReaderBuilder<SessionData>()
             .name("retentionReader")
             .entityManagerFactory(entityManagerFactory)
             .pageSize(CHUNK_SIZE)
            .queryString("SELECT s FROM SessionData s " +
                "WHERE s.id.collectedAt < :saveDate ")
             .parameterValues(Map.of("saveDate", saveDate))
             .saveState(false)
             .build();
     }
// @Bean
// @StepScope
// public JdbcPagingItemReader<CsvFileData> retentionFileReader(
//     @Value("#{jobParameters['saveDate']}") String saveDateStr) {
//     LocalDateTime saveDate = LocalDateTime.parse(saveDateStr);
//     log.info(">>> [ 🔍 보관 대상 데이터 조회 시작 - 기준일: {} ]", saveDate);

//     OraclePagingQueryProvider queryProvider = new OraclePagingQueryProvider();
//     queryProvider.setSelectClause("""
//         /*+ INDEX(s IDX_SESSION_COLLECTED_AT) */
//         s.device_id, s.sid, s.collected_at, s.serial,
//         s.username, s.command, s.command_name, s.status,
//         s.schemaname as schema_name, s.osuser as os_user,
//         s.process, s.machine, s.port, s.terminal,
//         s.program, s.type, s.sql_id, s.sql_exec_start,
//         s.sql_exec_id, s.sql_text, s.module, s.action,
//         s.logon_time, s.last_call_et, s.failed_over,
//         s.blocking_session_status, s.event, s.wait_class,
//         s.state, s.wait_time_micro, s.time_remaining_micro,
//         s.service_name, d.company_id
//         """);
//     queryProvider.setFromClause("SESSION_DATA s INNER JOIN DEVICES d ON s.device_id = d.device_id");
//     queryProvider.setWhereClause("s.collected_at < :saveDate");

//     Map<String, Order> sortKeys = new LinkedHashMap<>();
//     sortKeys.put("company_id", Order.ASCENDING);
//     sortKeys.put("device_id", Order.ASCENDING);
//     queryProvider.setSortKeys(sortKeys);

//     return new JdbcPagingItemReaderBuilder<CsvFileData>()
//         .name("retentionReader")
//         .dataSource(dataSource)
//         .queryProvider(queryProvider)
//         .parameterValues(Map.of("saveDate", saveDate))
//         .pageSize(CHUNK_SIZE)
//         .rowMapper((rs, rowNum) -> {
//             CsvFileData data = new CsvFileData();
//             data.setDeviceId(rs.getLong("device_id"));
//             data.setSid(Long.valueOf(rs.getString("sid")));
//             data.setCollectedAt(rs.getTimestamp("collected_at").toLocalDateTime());
//             data.setSerial(rs.getLong("serial"));
//             data.setUsername(rs.getString("username"));
//             data.setCommand(rs.getLong("command"));
//             data.setCommandName(rs.getString("command_name"));
//             data.setStatus(rs.getString("status"));
//             data.setSchemaName(rs.getString("schema_name"));
//             data.setOsUser(rs.getString("os_user"));
//             data.setProcess(rs.getString("process"));
//             data.setMachine(rs.getString("machine"));
//             data.setPort(rs.getInt("port"));
//             data.setTerminal(rs.getString("terminal"));
//             data.setProgram(rs.getString("program"));
//             data.setType(rs.getString("type"));
//             data.setSqlId(rs.getString("sql_id"));

//             Timestamp sqlExecStart = rs.getTimestamp("sql_exec_start");
//             data.setSqlExecStart(sqlExecStart != null ? sqlExecStart.toLocalDateTime() : null);

//             data.setSqlExecId(rs.getLong("sql_exec_id"));
//             data.setSqlText(rs.getString("sql_text"));
//             data.setModule(rs.getString("module"));
//             data.setAction(rs.getString("action"));

//             Timestamp logonTime = rs.getTimestamp("logon_time");
//             data.setLogonTime(logonTime != null ? logonTime.toLocalDateTime() : null);

//             data.setLastCallEt(rs.getLong("last_call_et"));
//             data.setFailedOver(rs.getString("failed_over"));
//             data.setBlockingSessionStatus(rs.getString("blocking_session_status"));
//             data.setEvent(rs.getString("event"));
//             data.setWaitClass(rs.getString("wait_class"));
//             data.setState(rs.getString("state"));
//             data.setWaitTimeMicro(rs.getLong("wait_time_micro"));
//             data.setTimeRemainingMicro(rs.getLong("time_remaining_micro"));
//             data.setServiceName(rs.getString("service_name"));
//             data.setCompanyId(rs.getLong("company_id"));
//             return data;
//         })
//         .build();
// }
//@Bean
//@StepScope
//public JdbcPagingItemReader<CsvFileData> retentionFileReader(
//    @Value("#{jobParameters['saveDate']}") String saveDateStr) {
//    LocalDateTime saveDate = LocalDateTime.parse(saveDateStr);
//    log.info(">>> [ 🔍 보관 대상 데이터 조회 시작 - 기준일: {} ]", saveDate);
//
//    // DEVICES 테이블 전체를 메모리에 로드
//    String deviceQuery = "SELECT device_id, company_id FROM DEVICES";
//    Map<Long, Long> deviceCompanyMap = jdbcTemplate.query(
//        deviceQuery,
//        (rs, rowNum) -> Map.entry(
//            rs.getLong("device_id"),
//            rs.getLong("company_id")
//        )
//    ).stream().collect(Collectors.toMap(
//        Map.Entry::getKey,
//        Map.Entry::getValue,
//        (existing, replacement) -> existing,  // 중복 키가 있을 경우 처리
//        ConcurrentHashMap::new  // 스레드 안전성 보장
//    ));
//
//    log.info(">>> [ 📊 디바이스-회사 매핑 로드 완료 - 총 {}개 ]", deviceCompanyMap.size());
//
//    OraclePagingQueryProvider queryProvider = new OraclePagingQueryProvider();
//    queryProvider.setSelectClause("""
//        /*+ INDEX(s IDX_SESSION_COLLECTED_AT) */
//        s.device_id, s.sid, s.collected_at, s.serial,
//        s.username, s.command, s.command_name, s.status,
//        s.schemaname as schema_name, s.osuser as os_user,
//        s.process, s.machine, s.port, s.terminal,
//        s.program, s.type, s.sql_id, s.sql_exec_start,
//        s.sql_exec_id, s.sql_text, s.module, s.action,
//        s.logon_time, s.last_call_et, s.failed_over,
//        s.blocking_session_status, s.event, s.wait_class,
//        s.state, s.wait_time_micro, s.time_remaining_micro,
//        s.service_name
//        """);
//
//    queryProvider.setFromClause("SESSION_DATA s");
//    queryProvider.setWhereClause("s.collected_at < :saveDate");
//
//    // 인덱스 활용을 위한 정렬 순서 설정
//    Map<String, Order> sortKeys = new LinkedHashMap<>();
//    sortKeys.put("collected_at", Order.ASCENDING);
//    sortKeys.put("device_id", Order.ASCENDING);
//    queryProvider.setSortKeys(sortKeys);
//
//    return new JdbcPagingItemReaderBuilder<CsvFileData>()
//        .name("retentionReader")
//        .dataSource(dataSource)
//        .queryProvider(queryProvider)
//        .parameterValues(Map.of("saveDate", saveDate))
//        .pageSize(CHUNK_SIZE)
//        .rowMapper((rs, rowNum) -> {
//            CsvFileData data = new CsvFileData();
//            Long deviceId = rs.getLong("device_id");
//            data.setDeviceId(deviceId);
//            data.setCompanyId(deviceCompanyMap.get(deviceId));
//            data.setSid(Long.valueOf(rs.getString("sid")));
//            data.setCollectedAt(rs.getTimestamp("collected_at").toLocalDateTime());
//            data.setSerial(rs.getLong("serial"));
//            data.setUsername(rs.getString("username"));
//            data.setCommand(rs.getLong("command"));
//            data.setCommandName(rs.getString("command_name"));
//            data.setStatus(rs.getString("status"));
//            data.setSchemaName(rs.getString("schema_name"));
//            data.setOsUser(rs.getString("os_user"));
//            data.setProcess(rs.getString("process"));
//            data.setMachine(rs.getString("machine"));
//            data.setPort(rs.getInt("port"));
//            data.setTerminal(rs.getString("terminal"));
//            data.setProgram(rs.getString("program"));
//            data.setType(rs.getString("type"));
//            data.setSqlId(rs.getString("sql_id"));
//
//            Timestamp sqlExecStart = rs.getTimestamp("sql_exec_start");
//            data.setSqlExecStart(sqlExecStart != null ? sqlExecStart.toLocalDateTime() : null);
//
//            data.setSqlExecId(rs.getLong("sql_exec_id"));
//            data.setSqlText(rs.getString("sql_text"));
//            data.setModule(rs.getString("module"));
//            data.setAction(rs.getString("action"));
//
//            Timestamp logonTime = rs.getTimestamp("logon_time");
//            data.setLogonTime(logonTime != null ? logonTime.toLocalDateTime() : null);
//
//            data.setLastCallEt(rs.getLong("last_call_et"));
//            data.setFailedOver(rs.getString("failed_over"));
//            data.setBlockingSessionStatus(rs.getString("blocking_session_status"));
//            data.setEvent(rs.getString("event"));
//            data.setWaitClass(rs.getString("wait_class"));
//            data.setState(rs.getString("state"));
//            data.setWaitTimeMicro(rs.getLong("wait_time_micro"));
//            data.setTimeRemainingMicro(rs.getLong("time_remaining_micro"));
//            data.setServiceName(rs.getString("service_name"));
//
//            return data;
//        })
//        .build();
//}
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
    public Step backupStep(
        JobRepository jobRepository,
        PlatformTransactionManager transactionManager) {
        return new StepBuilder("backupStep", jobRepository)
//            .<CsvFileData, CsvFileData>chunk(CHUNK_SIZE, transactionManager)
            .<SessionData, SessionData>chunk(CHUNK_SIZE, transactionManager)
            .reader(retentionFileReader(null))
                // .writer(retentionFileWriter())
            .writer(companyPartitionedFileWriter())
//            .taskExecutor(retentionTaskExecutor())
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
