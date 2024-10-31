package sandbox.semo.domain.company.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sandbox.semo.domain.common.entity.BaseForm;
import sandbox.semo.domain.common.entity.FormStatus;


@Entity
@Getter
@Table(name = "COMPANY_FORM")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CompanyForm extends BaseForm {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "COMPANY_REGISTER_SEQ_GEN")
    @SequenceGenerator(name = "COMPANY_REGISTER_SEQ_GEN", sequenceName = "COMPANY_FORM_SEQ", allocationSize = 1)
    @Column(name = "FORM_ID", nullable = false)
    private Long id;

    @Column(name = "TAX_ID", nullable = false, unique = true, length = 30)
    private String taxId;

    @Column(name = "COMPANY_NAME", nullable = false, length = 50)
    protected String companyName;

    @Builder
    public CompanyForm(String companyName, String taxId, String ownerName, String email,
            FormStatus formStatus) {
        this.companyName = companyName;
        this.taxId = taxId;
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
