package sandbox.semo.domain.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import sandbox.semo.domain.common.entity.BaseForm;
import sandbox.semo.domain.common.entity.FormStatus;
import sandbox.semo.domain.company.entity.Company;

@Entity
@Getter
@Table(name = "MEMBER_FORM")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberForm extends BaseForm {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "REGISTER_SEQ_GEN")
    @SequenceGenerator(name = "REGISTER_SEQ_GEN", sequenceName = "MEMBER_FORM_SEQ", allocationSize = 1)
    @Column(name = "FORM_ID", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COMPANY_ID", nullable = false)
    private Company company;


    @Builder
    public MemberForm(Company company, String ownerName, String email,
            FormStatus formStatus) {
        this.company = company;
        this.ownerName = ownerName;
        this.email = email;
        this.formStatus = formStatus;
    }

    public void changeStatus(FormStatus newFormStatus) {
        if (newFormStatus == FormStatus.APPROVED) {
            this.markAsApproved();
        }
        this.formStatus = newFormStatus;
    }

}
