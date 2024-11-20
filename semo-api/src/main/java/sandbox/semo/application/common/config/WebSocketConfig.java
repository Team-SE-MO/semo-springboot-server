package sandbox.semo.application.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import sandbox.semo.application.monitoring.socket.BatchDataMonitoringWebSocketHandler;
import sandbox.semo.application.monitoring.socket.SessionDataMonitoringWebSocketHandler;
import sandbox.semo.application.monitoring.socket.WebSocketHandShakeInterceptor;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final SessionDataMonitoringWebSocketHandler sessionDataHandler;
    private final BatchDataMonitoringWebSocketHandler batchDataHandler;
    private final WebSocketHandShakeInterceptor webSocketInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(sessionDataHandler, "/ws/monitoring/{companyId}/{deviceAlias}")
                .setAllowedOrigins("*")
                .addInterceptors(webSocketInterceptor);

        registry.addHandler(batchDataHandler, "/ws/monitoring/batch")
                .setAllowedOrigins("*");
    }

}
