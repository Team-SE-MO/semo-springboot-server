package sandbox.semo.application.company.service;

import java.util.List;
import sandbox.semo.domain.company.entity.Company;

public interface CompanyService {

    Long companyRegister(Long formId);

    List<Company> searchCompanyByName(String keyword);
}
