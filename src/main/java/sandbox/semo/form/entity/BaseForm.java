package sandbox.semo.form.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseForm {

    @Column(name = "OWNER_NAME", nullable = false, length = 50)
    protected String ownerName;

    @Column(name = "EMAIL", nullable = false, length = 50)
    protected String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    protected Status status;

    @Column(name = "COMPANY_NAME", nullable = false, length = 50)
    protected String companyName;

    @CreatedDate
    @Column(name = "REQUEST_DATE", nullable = false, updatable = false)
    private LocalDateTime requestDate;

    @Column(name = "APPROVED_AT", nullable = true)
    private LocalDateTime approvedAt;

    public void markAsApproved() {
        this.approvedAt = LocalDateTime.now();  // 현재 시간 설정
    }

}
