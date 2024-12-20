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

@Entity
@Getter
@Table(name = "COMPANIES")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "COMPANY_SEQ_GEN")
    @SequenceGenerator(name = "COMPANY_SEQ_GEN", sequenceName = "COMPANY_SEQ", allocationSize = 1)
    @Column(name = "COMPANY_ID", nullable = false)
    private Long id;

    @Column(name = "COMPANY_NAME", nullable = false, length = 50)
    private String companyName;

    @Column(name = "TAX_ID", nullable = false, unique = true, length = 30)
    private String taxId;

    @Builder
    public Company(String companyName, String taxId) {
        this.companyName = companyName;
        this.taxId = taxId;
    }

}
