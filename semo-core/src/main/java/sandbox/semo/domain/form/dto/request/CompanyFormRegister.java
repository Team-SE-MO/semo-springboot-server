package sandbox.semo.domain.form.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CompanyFormRegister {

    @NotNull
    private String companyName;

    @NotBlank
    private String taxId;

    @NotBlank
    private String email;

    @NotBlank
    private String ownerName;
}
