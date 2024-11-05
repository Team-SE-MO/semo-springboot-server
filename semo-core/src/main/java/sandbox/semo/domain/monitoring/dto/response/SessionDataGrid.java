package sandbox.semo.domain.monitoring.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionDataGrid {

    private LocalDateTime collectedAt;

    private Long sid;

    private String action;

    private String blockingSessionStatus;

    private Long command;

    private String commandName;

    private String event;

    private String failedOver;

    private Long lastCallEt;

    private LocalDateTime logonTime;

    private String machine;

    private String module;

    private String osUser;

    private Integer port;

    private String process;

    private String program;

    private String schemaName;

    private Long serial;

    private String serviceName;

    private Long sqlExecId;

    private LocalDateTime sqlExecStart;

    private String sqlId;

    private String sqlText;

    private String state;

    private String status;

    private String terminal;

    private Long timeRemainingMicro;

    private String type;

    private String username;

    private String waitClass;

    private Long waitTimeMicro;

    private Long device;

}
