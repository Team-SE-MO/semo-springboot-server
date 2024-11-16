package sandbox.semo.application.company.service;

import java.util.List;
import sandbox.semo.domain.common.dto.response.FormDecisionResponse;
import sandbox.semo.domain.common.dto.response.OffsetPage;
import sandbox.semo.domain.company.dto.request.CompanyFormDecision;
import sandbox.semo.domain.company.dto.request.CompanyFormRegister;
import sandbox.semo.domain.company.dto.response.CompanyFormInfo;
import sandbox.semo.domain.company.entity.Company;

public interface CompanyService {

    Long companyRegister(Long formId);

    List<Company> searchCompanyByName(String keyword);

    void formRegister(CompanyFormRegister request);

    OffsetPage<CompanyFormInfo> findForms(int page, int size);

    FormDecisionResponse updateStatus(CompanyFormDecision request);
}
