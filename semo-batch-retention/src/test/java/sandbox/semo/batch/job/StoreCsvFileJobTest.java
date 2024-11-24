package sandbox.semo.batch.job;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.task.TaskExecutor;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import sandbox.semo.batch.service.step.CompanyPartitionedFileWriter;
import sandbox.semo.batch.service.step.DailyTimeRangePartitioner;
import sandbox.semo.batch.service.step.SessionDataFileWriter;
import sandbox.semo.batch.service.tasklet.S3UploadTasklet;
import sandbox.semo.domain.monitoring.dto.request.CsvFileData;
import sandbox.semo.domain.monitoring.entity.SessionData;

@SpringBootTest
@Slf4j
public class StoreCsvFileJobTest {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private CompanyPartitionedFileWriter companyPartitionedFileWriter;

    @Autowired
    private JdbcPagingItemReader<CsvFileData> storeCsvFileReader;

    @Autowired
    private S3UploadTasklet s3UploadTasklet;

    @Autowired
    @Qualifier("storeCsvFileTaskExecutor")
    private TaskExecutor taskExecutor;
    private static final int CHUNK_SIZE = 30000;
    private static final int GRID_SIZE = 6;
    @Autowired
    private LocalContainerEntityManagerFactoryBean entityManagerFactory;

    @Value("${backup.path}")
    private String backupBasePath;

    @Test
    public void compareJobPerformance() throws Exception {
        String saveDate = LocalDateTime.now().minusDays(1).toString();

        Job singleThreadJob = createJob(false, saveDate);
        Job multiThreadJob = createJob(true, saveDate);

        // 싱글스레드 테스트
        log.info("=== 싱글스레드 테스트 시작 ===");
        JobParameters singleParams = new JobParametersBuilder()
            .addString("saveDate", saveDate)
            .addLong("timestamp", System.currentTimeMillis())
            .toJobParameters();

        long singleStartTime = System.currentTimeMillis();
        jobLauncher.run(singleThreadJob, singleParams);
        long singleExecutionTime = System.currentTimeMillis() - singleStartTime;

        Thread.sleep(1000);

        // 멀티스레드 테스트
        log.info("=== 멀티스레드 테스트 시작 ===");
        JobParameters multiParams = new JobParametersBuilder()
            .addString("saveDate", saveDate)
            .addLong("timestamp", System.currentTimeMillis())
            .toJobParameters();

        long multiStartTime = System.currentTimeMillis();
        jobLauncher.run(multiThreadJob, multiParams);
        long multiExecutionTime = System.currentTimeMillis() - multiStartTime;

        // 결과 비교
        double improvement =
            ((double) (singleExecutionTime - multiExecutionTime) / singleExecutionTime) * 100;
        log.info("=== 성능 비교 결과 ===");
        log.info("싱글스레드 실행 시간: {} ms", singleExecutionTime);
        log.info("멀티스레드 실행 시간: {} ms", multiExecutionTime);
        log.info("성능 향상률: {}", String.format("%.2f%%", improvement));
    }

    private Job createJob(boolean isMultiThread, String saveDate) {
        // Slave Step 생성
        Step slaveStep = new StepBuilder("csvFileSlaveStep", jobRepository)
            .<CsvFileData, CsvFileData>chunk(CHUNK_SIZE, transactionManager)
            .reader(storeCsvFileReader)
            .writer(companyPartitionedFileWriter)
            .build();

        // Partitioner 생성
        Partitioner partitioner = new DailyTimeRangePartitioner(saveDate);

        // Master Step 생성
        Step masterStep;
        StepBuilder masterStepBuilder = new StepBuilder("csvFileMasterStep", jobRepository);

        if (isMultiThread) {
            masterStep = masterStepBuilder
                .partitioner(slaveStep.getName(), partitioner)
                .step(slaveStep)
                .gridSize(GRID_SIZE)
                .taskExecutor(taskExecutor)
                .build();
        } else {
            masterStep = masterStepBuilder
                .partitioner(slaveStep.getName(), partitioner)
                .step(slaveStep)
                .gridSize(GRID_SIZE)
                .build();
        }

        // S3 Upload Step 생성
        Step s3Step = new StepBuilder("s3UploadStep", jobRepository)
            .tasklet(s3UploadTasklet, transactionManager)
            .build();

        // Job 생성
        return new JobBuilder(isMultiThread ? "multiThreadJob" : "singleThreadJob", jobRepository)
            .start(masterStep)
            .next(s3Step)
            .build();
    }

    @Test
    public void compareJdbcAndJpaPerformance() throws Exception {
        String saveDate = LocalDateTime.now().minusDays(1).toString();

        // JDBC Job 실행
        Job jdbcJob = createJdbcJob(saveDate);
        long jdbcTime = runJob("JDBC", jdbcJob);

        Thread.sleep(1000);

        // JPA Job 실행
        Job jpaJob = createJpaJob(saveDate);
        long jpaTime = runJob("JPA", jpaJob);

        // 결과 출력
        log.info("=== 성능 비교 결과 ===");
        log.info("JDBC 실행 시간: {} ms", jdbcTime);
        log.info("JPA 실행 시간: {} ms", jpaTime);
        double improvementRate = ((double) jpaTime - jdbcTime) / jpaTime * 100;
        log.info("성능 향상률: {}%", String.format("%.2f", improvementRate));
    }

    private long runJob(String type, Job job) throws Exception {
        JobParameters params = new JobParametersBuilder()
            .addString("saveDate", LocalDateTime.now().minusDays(7).toString())
            .addLong("timestamp", System.currentTimeMillis())
            .toJobParameters();

        log.info("=== {} 테스트 시작 ===", type);
        long startTime = System.currentTimeMillis();
        jobLauncher.run(job, params);
        long executionTime = System.currentTimeMillis() - startTime;
        log.info("{} 실행 시간: {} ms", type, executionTime);
        return executionTime;
    }

    private Job createJdbcJob(String saveDate) {
        Step slaveStep = new StepBuilder("csvFileSlaveStep", jobRepository)
            .<CsvFileData, CsvFileData>chunk(CHUNK_SIZE, transactionManager)
            .reader(storeCsvFileReader)
            .writer(companyPartitionedFileWriter)  // 기존 JDBC용 writer 사용
            .build();

        return createPartitionedJob("jdbcJob", slaveStep, saveDate);
    }

    private Job createJpaJob(String saveDate) {
        Map<String, Object> parameterValues = new HashMap<>();
        LocalDateTime parsedDate = LocalDateTime.parse(saveDate);
        String dateOnly = parsedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        parameterValues.put("saveDate", dateOnly);

        JpaPagingItemReader<SessionData> jpaReader = new JpaPagingItemReaderBuilder<SessionData>()
            .name("jpaSessionDataReader")
            .entityManagerFactory(entityManagerFactory.getObject())
            .queryString(
                "SELECT s FROM SessionData s " +
                    "JOIN FETCH s.device d " +
                    "JOIN FETCH d.company " +
                    "WHERE TO_CHAR(s.id.collectedAt, 'YYYY-MM-DD') = :saveDate " +
                    "ORDER BY d.id")
            .parameterValues(parameterValues)  // 파라미터 바인딩 추가
            .pageSize(CHUNK_SIZE)
            .saveState(false)
            .build();

        Step slaveStep = new StepBuilder("sessionDataSlaveStep", jobRepository)
            .<SessionData, SessionData>chunk(CHUNK_SIZE, transactionManager)
            .reader(jpaReader)
            .writer(new SessionDataFileWriter(backupBasePath, saveDate))  // 새로 만든 JPA용 writer 사용
            .build();

        return createPartitionedJob("jpaJob", slaveStep, saveDate);
    }

    private Job createPartitionedJob(String jobName, Step slaveStep, String saveDate) {
        Partitioner partitioner = new DailyTimeRangePartitioner(saveDate);

        Step masterStep = new StepBuilder("masterStep", jobRepository)
            .partitioner(slaveStep.getName(), partitioner)
            .step(slaveStep)
            .gridSize(GRID_SIZE)
            .build();

        Step s3Step = new StepBuilder("s3UploadStep", jobRepository)
            .tasklet(s3UploadTasklet, transactionManager)
            .build();

        return new JobBuilder(jobName, jobRepository)
            .start(masterStep)
            .next(s3Step)
            .build();
    }
}
