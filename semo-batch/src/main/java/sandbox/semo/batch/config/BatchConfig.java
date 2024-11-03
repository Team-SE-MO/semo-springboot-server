package sandbox.semo.batch.config;

import jakarta.persistence.EntityManagerFactory;
import java.time.LocalDateTime;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import sandbox.semo.batch.dto.DeviceInfo;
import sandbox.semo.batch.repository.JdbcRepository;
import sandbox.semo.batch.service.step.DeviceProcessor;
import sandbox.semo.batch.service.step.DeviceReaderListener;
import sandbox.semo.batch.service.step.DeviceWriter;
import sandbox.semo.domain.common.crypto.AES256;
import sandbox.semo.domain.device.entity.Device;

@Log4j2
@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    private final JdbcRepository jdbcRepository;
    private final AES256 aes256;
    private final EntityManagerFactory entityManagerFactory;
    private final DeviceReaderListener deviceReaderListener;

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
            .pageSize(5)
            .queryString("SELECT d FROM Device d ORDER BY d.id")
            .saveState(false)
            .build();
    }


    @Bean
    @StepScope
    public ItemProcessor<Device, DeviceInfo> deviceProcessor() {
        LocalDateTime collectedAt = LocalDateTime.now().withNano(0);
        log.info(">>> [ ⏰ Job 시작 시간 설정: {} ]", collectedAt);
        return new DeviceProcessor(aes256, jdbcRepository, collectedAt);
    }

    @Bean
    @StepScope
    public ItemWriter<DeviceInfo> deviceWriter() {
        return new DeviceWriter(jdbcRepository);
    }

    @Bean
    protected Step deviceCollectionStep(
        JobRepository jobRepository, PlatformTransactionManager transactionManager,
        ItemReader<Device> reader,
        ItemProcessor<Device, DeviceInfo> processor,
        ItemWriter<DeviceInfo> writer
    ) {
        return new StepBuilder("deviceStatusValidStep", jobRepository)
            .<Device, DeviceInfo>chunk(5, transactionManager)
            .reader(reader)
            .processor(processor)
            .writer(writer)
            .listener((StepExecutionListener) deviceReaderListener)
            .listener((ItemReadListener<? super Device>) deviceReaderListener) // Step 리스너
            .taskExecutor(deviceTaskExecutor())
            .build();
    }

    @Bean
    public Job job(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new JobBuilder("chunksJob", jobRepository)
            .start(deviceCollectionStep(
                jobRepository, transactionManager,
                deviceReader(), deviceProcessor(), deviceWriter()
            ))
            .build();
    }

}

