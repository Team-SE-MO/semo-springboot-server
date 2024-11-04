package sandbox.semo.domain.monitoring.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sandbox.semo.domain.device.entity.Device;

@Entity
@Getter
@Builder
@Table(name = "SESSION_DATA")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SessionData {

    @EmbeddedId
    private SessionDataId id;

    @ManyToOne
    @MapsId("deviceId")
    @JoinColumn(name = "DEVICE_ID", nullable = false)
    private Device device;

    @Column(name = "SERIAL")
    private Long serial;

    @Column(name = "USERNAME", length = 30)
    private String username;

    @Column(name = "COMMAND")
    private Long command;

    @Column(name = "COMMAND_NAME", length = 64)
    private String commandName;

    @Column(name = "STATUS", length = 8)
    private String status;

    @Column(name = "SCHEMANAME", length = 30)
    private String schemaName;

    @Column(name = "OSUSER", length = 30)
    private String osUser;

    @Column(name = "PROCESS", length = 12)
    private String process;

    @Column(name = "MACHINE", length = 64)
    private String machine;

    @Column(name = "PORT")
    private Integer port;

    @Column(name = "TERMINAL", length = 30)
    private String terminal;

    @Column(name = "PROGRAM", length = 48)
    private String program;

    @Column(name = "TYPE", length = 10)
    private String type;

    @Column(name = "SQL_ID", length = 13)
    private String sqlId;

    @Column(name = "SQL_EXEC_START")
    private LocalDateTime sqlExecStart;

    @Column(name = "SQL_EXEC_ID")
    private Long sqlExecId;

    @Column(name = "SQL_TEXT", length = 200)
    private String sqlText;

    @Column(name = "MODULE", length = 48)
    private String module;

    @Column(name = "ACTION", length = 64)
    private String action;

    @Column(name = "LOGON_TIME")
    private LocalDateTime logonTime;

    @Column(name = "LAST_CALL_ET")
    private Long lastCallEt;

    @Column(name = "FAILED_OVER", length = 3)
    private String failedOver;

    @Column(name = "BLOCKING_SESSION_STATUS", length = 13)
    private String blockingSessionStatus;

    @Column(name = "EVENT", length = 64)
    private String event;

    @Column(name = "WAIT_CLASS", length = 64)
    private String waitClass;

    @Column(name = "STATE", length = 19)
    private String state;

    @Column(name = "WAIT_TIME_MICRO")
    private Long waitTimeMicro;

    @Column(name = "TIME_REMAINING_MICRO")
    private Long timeRemainingMicro;

    @Column(name = "SERVICE_NAME", length = 30)
    private String serviceName;

}
