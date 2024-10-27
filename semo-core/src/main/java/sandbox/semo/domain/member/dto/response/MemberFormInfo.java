package sandbox.semo.domain.member.dto.response;


import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sandbox.semo.domain.common.entity.FormStatus;
import sandbox.semo.domain.company.entity.Company;

@Getter
@NoArgsConstructor
public class MemberFormInfo {

    private Long formId;
    private Company company;
    private String ownerName;
    private String email;
    private FormStatus formStatus;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime requestDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime approvedAt;

    @Builder
    public MemberFormInfo(Long formId, Company company, String ownerName,
            String email, FormStatus formStatus, LocalDateTime requestDate,
            LocalDateTime approvedAt) {
        this.formId = formId;
        this.company = company;
        this.ownerName = ownerName;
        this.email = email;
        this.formStatus = formStatus;
        this.requestDate = requestDate;
        this.approvedAt = approvedAt;
    }
}
