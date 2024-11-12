package sandbox.semo.application.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import sandbox.semo.application.monitoring.socket.MonitoringWebSocketHandler;
import sandbox.semo.application.monitoring.socket.WebSocketHandShakeInterceptor;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final MonitoringWebSocketHandler webSocketHandler;
    private final WebSocketHandShakeInterceptor webSocketInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketHandler, "/ws/monitoring/{companyId}/{deviceAlias}")
                .setAllowedOrigins("*")
                .addInterceptors(webSocketInterceptor);
    }
}
