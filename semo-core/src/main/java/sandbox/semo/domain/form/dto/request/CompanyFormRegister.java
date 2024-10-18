package sandbox.semo.domain.form.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CompanyFormRegister {

    @NotNull
    private String companyName;

    @NotNull
    private String taxId;

    @NotNull
    private String email;

    @NotNull
    private String ownerName;
}
