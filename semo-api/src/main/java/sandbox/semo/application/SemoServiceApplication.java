package sandbox.semo.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "sandbox.semo.domain")
@ComponentScan(basePackages = {"sandbox.semo"})
@EntityScan(basePackages = "sandbox.semo.domain")
public class SemoServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SemoServiceApplication.class, args);
    }

}
