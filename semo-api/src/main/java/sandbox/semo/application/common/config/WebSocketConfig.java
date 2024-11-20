package sandbox.semo.application.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import sandbox.semo.application.monitoring.socket.BatchDataMonitoringWebSocketHandler;
import sandbox.semo.application.monitoring.socket.BatchDataWebSocketHandShakeInterceptor;
import sandbox.semo.application.monitoring.socket.SessionDataMonitoringWebSocketHandler;
import sandbox.semo.application.monitoring.socket.SessionDataWebSocketHandShakeInterceptor;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final SessionDataMonitoringWebSocketHandler sessionDataHandler;
    private final SessionDataWebSocketHandShakeInterceptor sessionDataWebSocketHandShakeInterceptor;
    private final BatchDataMonitoringWebSocketHandler batchDataHandler;
    private final BatchDataWebSocketHandShakeInterceptor batchDataWebSocketHandShakeInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(sessionDataHandler, "/ws/monitoring/{companyId}/{deviceAlias}")
                .setAllowedOrigins("*")
                .addInterceptors(sessionDataWebSocketHandShakeInterceptor);

        registry.addHandler(batchDataHandler, "/ws/monitoring/batch")
                .setAllowedOrigins("*")
                .addInterceptors(batchDataWebSocketHandShakeInterceptor);
    }

}
