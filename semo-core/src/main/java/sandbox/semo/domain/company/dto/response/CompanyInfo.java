package sandbox.semo.domain.company.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import sandbox.semo.domain.company.entity.Company;

@Data
@AllArgsConstructor
public class CompanyInfo {

    List<Company> companies;

}
