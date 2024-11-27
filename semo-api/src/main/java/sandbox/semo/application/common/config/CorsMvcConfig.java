package sandbox.semo.application.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsMvcConfig implements WebMvcConfigurer {

    @Value("${deploy.ip}")
    private String ip;

    @Override
    public void addCorsMappings(CorsRegistry corsRegistry) {
        String url = "http://" + ip + ":3000";
        corsRegistry.addMapping("/**")
            .allowedOrigins(url);
    }
}
