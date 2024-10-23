package sandbox.semo.domain.form.dto.response;


import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sandbox.semo.domain.form.entity.Status;

@Getter
@NoArgsConstructor
public class MemberFormInfo {

    private Long formId;
    private String companyName;
    private String ownerName;
    private String email;
    private Status status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime requestDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime approvedAt;

    @Builder
    public MemberFormInfo(Long formId, String companyName, String ownerName,
            String email, Status status, LocalDateTime requestDate, LocalDateTime approvedAt) {
        this.formId = formId;
        this.companyName = companyName;
        this.ownerName = ownerName;
        this.email = email;
        this.status = status;
        this.requestDate = requestDate;
        this.approvedAt = approvedAt;
    }
}
