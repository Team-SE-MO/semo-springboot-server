package sandbox.semo.domain.monitoring.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sandbox.semo.domain.monitoring.entity.SessionData;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionDataInfo {

    private LocalDateTime collectedAt;
    private Long sid;
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

    public static SessionDataInfo fromEntity(SessionData sessionData) {
        return SessionDataInfo.builder()
                .collectedAt(sessionData.getId().getCollectedAt())
                .sid(sessionData.getId().getSid())
                .serial(sessionData.getSerial())
                .username(sessionData.getUsername())
                .command(sessionData.getCommand())
                .commandName(sessionData.getCommandName())
                .status(sessionData.getStatus())
                .schemaName(sessionData.getSchemaName())
                .osUser(sessionData.getOsUser())
                .process(sessionData.getProcess())
                .machine(sessionData.getMachine())
                .port(sessionData.getPort())
                .terminal(sessionData.getTerminal())
                .program(sessionData.getProgram())
                .type(sessionData.getType())
                .sqlId(sessionData.getSqlId())
                .sqlExecStart(sessionData.getSqlExecStart())
                .sqlExecId(sessionData.getSqlExecId())
                .sqlText(sessionData.getSqlText())
                .module(sessionData.getModule())
                .action(sessionData.getAction())
                .logonTime(sessionData.getLogonTime())
                .lastCallEt(sessionData.getLastCallEt())
                .failedOver(sessionData.getFailedOver())
                .blockingSessionStatus(sessionData.getBlockingSessionStatus())
                .event(sessionData.getEvent())
                .waitClass(sessionData.getWaitClass())
                .state(sessionData.getState())
                .waitTimeMicro(sessionData.getWaitTimeMicro())
                .timeRemainingMicro(sessionData.getTimeRemainingMicro())
                .serviceName(sessionData.getServiceName())
                .build();
    }

}
