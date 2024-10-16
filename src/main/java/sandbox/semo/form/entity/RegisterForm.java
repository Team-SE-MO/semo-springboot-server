package sandbox.semo.form.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@Table(name = "REGISTER_FORM")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RegisterForm {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "REGISTER_SEQ_GEN")
    @SequenceGenerator(name = "REGISTER_SEQ_GEN", sequenceName = "REGISTER_SEQ", allocationSize = 1)
    @Column(name = "FORM_ID", nullable = false)
    private Long id;

    @Column(name = "COMPANY_NAME", nullable = false, length = 50)
    private String companyName;

    @Column(name = "OWNER_NAME", nullable = false, length = 50)
    private String ownerName;

    @Column(name = "EMAIL", nullable = false, length = 50)
    private String email;

    @Column(name = "STATUS", nullable = false, length = 30)
    private String status;

    @CreatedDate
    @Column(name = "REQUEST_DATE", nullable = false, updatable = false)
    private LocalDateTime requestDate;

    @Column(name = "APPROVED_AT", nullable = true)
    private LocalDateTime approvedAt;

    public void markAsApproved() {
        this.approvedAt = LocalDateTime.now();  // 현재 시간 설정
    }

    @Builder
    public RegisterForm(String companyName, String ownerName, String email, String status) {
        this.companyName = companyName;
        this.ownerName = ownerName;
        this.email = email;
        this.status = status;
    }
}
