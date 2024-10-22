package sandbox.semo.domain.form.dto.response;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CompanyRegister {

    @NotNull
    private String companyName;

    @NotNull
    private String email;

    @NotNull
    private String ownerName;

    public CompanyRegister(String companyName, String email, String ownerName) {
        this.companyName = companyName;
        this.email = email;
        this.ownerName = ownerName;
    }
}
