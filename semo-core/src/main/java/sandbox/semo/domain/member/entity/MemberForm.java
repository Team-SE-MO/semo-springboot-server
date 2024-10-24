package sandbox.semo.domain.member.entity;

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
@Table(name = "MEMBER_FORM")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberForm extends BaseForm {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "REGISTER_SEQ_GEN")
    @SequenceGenerator(name = "REGISTER_SEQ_GEN", sequenceName = "MEMBER_FORM_SEQ", allocationSize = 1)
    @Column(name = "FORM_ID", nullable = false)
    private Long id;

    @Builder
    public MemberForm(String companyName, String ownerName, String email, FormStatus formStatus) {
        this.companyName = companyName;
        this.ownerName = ownerName;
        this.email = email;
        this.formStatus = formStatus;
    }

    public void changeStatus(FormStatus newFormStatus) {
        this.formStatus = newFormStatus;
        this.markAsApproved();
    }

}
