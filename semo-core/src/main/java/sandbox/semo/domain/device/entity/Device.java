package sandbox.semo.domain.device.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sandbox.semo.domain.company.entity.Company;
import sandbox.semo.domain.common.entity.BaseTime;

@Entity
@Getter
@Table(name = "DEVICES")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Device extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "DEVICE_SEQ_GEN")
    @SequenceGenerator(name = "DEVICE_SEQ_GEN", sequenceName = "DEVICE_SEQ", allocationSize = 1)
    @Column(name = "DEVICE_ID", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COMPANY_ID", nullable = false)
    private Company company;

    @Column(name = "DEVICE_ALIAS", nullable = false, length = 100)
    private String deviceAlias;

    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE", nullable = false, length = 30)
    private DatabaseType type;

    @Column(name = "IP", nullable = false, length = 100)
    private String ip;

    @Column(name = "PORT", nullable = false)
    private Long port;

    @Column(name = "SID", nullable = false, length = 50)
    private String sid;

    @Column(name = "USERNAME", nullable = false, length = 50)
    private String username;

    @Column(name = "PASSWORD", nullable = false)
    private String password;

    @Column(name = "STATUS", nullable = false)
    private Boolean status;

    @Builder
    public Device(
            Company company, String deviceAlias, DatabaseType type, String ip, Long port, String sid, String username,
            String password, Boolean status) {
        this.company = company;
        this.deviceAlias = deviceAlias;
        this.type = type;
        this.ip = ip;
        this.port = port;
        this.sid = sid;
        this.username = username;
        this.password = password;
        this.status = status;
    }

}
