package sandbox.semo.domain.company.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sandbox.semo.domain.common.entity.FormStatus;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CompanyFormInfo {

    private Long formId;
    private String companyName;
    private String taxId;
    private String ownerName;
    private String email;
    private FormStatus formStatus;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime requestDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime approvedAt;

    @Builder
    public CompanyFormInfo(Long formId, String companyName, String taxId, String ownerName,
            String email, FormStatus formStatus, LocalDateTime requestDate,
            LocalDateTime approvedAt) {
        this.formId = formId;
        this.companyName = companyName;
        this.taxId = taxId;
        this.ownerName = ownerName;
        this.email = email;
        this.formStatus = formStatus;
        this.requestDate = requestDate;
        this.approvedAt = approvedAt;
    }
}
