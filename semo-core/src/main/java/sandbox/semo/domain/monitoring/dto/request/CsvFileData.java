package sandbox.semo.domain.monitoring.dto.request;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CsvFileData {

    private LocalDateTime collectedAt;

    private Long sid;

    private Long deviceId;

    private Long serial;

    private String username;

    private Long command;

    private String commandName;

    private String status;

    private String schemaName;

    private String osUser;

    private String process;

    private String machine;

    private Integer port;

    private String terminal;

    private String program;

    private String type;

    private String sqlId;

    private LocalDateTime sqlExecStart;

    private Long sqlExecId;

    private String sqlText;

    private String module;

    private String action;

    private LocalDateTime logonTime;

    private Long lastCallEt;

    private String failedOver;

    private String blockingSessionStatus;

    private String event;

    private String waitClass;

    private String state;

    private Long waitTimeMicro;

    private Long timeRemainingMicro;

    private String serviceName;

    private Long companyId;
}
