package sandbox.semo.application.company.service;

import org.springframework.data.domain.Page;
import sandbox.semo.domain.company.dto.request.CompanyFormDecision;
import sandbox.semo.domain.company.dto.request.CompanyFormRegister;
import sandbox.semo.domain.company.dto.response.CompanyFormInfo;

public interface CompanyFormService {

    void formRegister(CompanyFormRegister companyFormRegister);

    Page<CompanyFormInfo> findAllForms(int page, int size);

    String updateStatus(CompanyFormDecision request);
}
