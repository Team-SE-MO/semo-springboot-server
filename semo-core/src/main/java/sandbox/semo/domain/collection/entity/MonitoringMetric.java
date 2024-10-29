package sandbox.semo.domain.collection.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sandbox.semo.domain.device.entity.Device;

@Entity
@Getter
@Builder
@Table(name = "MONITORING_METRICS")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MonitoringMetric {

    @EmbeddedId
    private MonitoringMetricId id;

    @ManyToOne
    @MapsId("deviceId")
    @JoinColumn(name = "DEVICE_ID", nullable = false)
    private Device device;

    @Column(name = "TOTAL_SESSION_COUNT")
    private Integer totalSessionCount;

    @Column(name = "ACTIVE_SESSION_COUNT")
    private Integer activeSessionCount;

    @Lob
    @Column(name = "SESSION_COUNT_GROUP_BY_USER")
    private String sessionCountGroupByUser;

    @Lob
    @Column(name = "SESSION_COUNT_GROUP_BY_COMMAND")
    private String sessionCountGroupByCommand;

    @Lob
    @Column(name = "SESSION_COUNT_GROUP_BY_MACHINE")
    private String sessionCountGroupByMachine;

    @Lob
    @Column(name = "SESSION_COUNT_GROUP_BY_TYPE")
    private String sessionCountGroupByType;

    @Lob
    @Column(name = "BLOCKING_SESSION_COUNT")
    private String blockingSessionCount;

    @Lob
    @Column(name = "WAIT_SESSION_COUNT")
    private String waitSessionCount;

}
