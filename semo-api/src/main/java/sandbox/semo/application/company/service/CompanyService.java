package sandbox.semo.application.company.service;

import java.util.List;
import org.springframework.data.domain.Page;
import sandbox.semo.domain.company.dto.request.CompanyFormDecision;
import sandbox.semo.domain.company.dto.request.CompanyFormRegister;
import sandbox.semo.domain.company.dto.response.CompanyFormInfo;
import sandbox.semo.domain.company.entity.Company;

public interface CompanyService {

    Long companyRegister(Long formId);

    List<Company> searchCompanyByName(String keyword);

    void formRegister(CompanyFormRegister request);

    Page<CompanyFormInfo> findAllForms(int page, int size);

    String updateStatus(CompanyFormDecision request);
}
