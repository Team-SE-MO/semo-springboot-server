package sandbox.semo.domain.form.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


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

    @Builder
    public CompanyForm(String companyName, String taxId, String ownerName, String email,
            Status status) {
        this.companyName = companyName;
        this.taxId = taxId;
        this.ownerName = ownerName;
        this.email = email;
        this.status = status;
    }

}
