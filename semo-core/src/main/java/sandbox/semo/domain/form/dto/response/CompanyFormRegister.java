package sandbox.semo.domain.form.dto.response;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CompanyFormRegister {

    @NotNull
    private String companyName;

    @NotNull
    private String email;

    @NotNull
    private String ownerName;

    public CompanyFormRegister(String companyName, String email, String ownerName) {
        this.companyName = companyName;
        this.email = email;
        this.ownerName = ownerName;
    }
}
