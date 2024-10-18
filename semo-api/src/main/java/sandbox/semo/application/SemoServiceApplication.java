package sandbox.semo.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "sandbox.semo.domain")
@EntityScan(basePackages = "sandbox.semo.domain")
public class SemoServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SemoServiceApplication.class, args);
    }

}
