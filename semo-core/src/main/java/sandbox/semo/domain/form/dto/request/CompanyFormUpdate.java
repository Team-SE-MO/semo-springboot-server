package sandbox.semo.domain.form.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CompanyFormUpdate {

    @NotNull
    private Long formId;

    @NotBlank
    private String updateStatus;
}
