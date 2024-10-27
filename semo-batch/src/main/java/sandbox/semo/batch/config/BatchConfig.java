package sandbox.semo.batch.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import sandbox.semo.batch.dto.DeviceInfo;
import sandbox.semo.batch.repository.JdbcRepository;
import sandbox.semo.batch.service.step.DeviceStatusProcessor;
import sandbox.semo.batch.service.step.DeviceReader;
import sandbox.semo.batch.service.step.DeviceStatusWriter;
import sandbox.semo.domain.common.crypto.AES256;
import sandbox.semo.domain.device.entity.Device;
import sandbox.semo.domain.device.repository.DeviceRepository;

@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    private final DeviceRepository jpaRepository;
    private final JdbcRepository jdbcRepository;
    private final AES256 aes256;

    @Bean
    public ItemReader<Device> deviceReader() {
        return new DeviceReader(jpaRepository);
    }

    @Bean
    public ItemProcessor<Device, DeviceInfo> deviceProcessor() {
        return new DeviceStatusProcessor(aes256, jdbcRepository);
    }

    @Bean
    public ItemWriter<DeviceInfo> deviceWriter() {
        return new DeviceStatusWriter(jdbcRepository);
    }

    @Bean
    protected Step deviceStatusValidStep(
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
                .build();
    }

    @Bean
    public Job job(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new JobBuilder("chunksJob", jobRepository)
                .start(deviceStatusValidStep(
                        jobRepository, transactionManager,
                        deviceReader(), deviceProcessor(), deviceWriter()
                ))
                .build();
    }

}
