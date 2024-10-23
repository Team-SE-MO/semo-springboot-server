package sandbox.semo.application.form.service;

import org.springframework.data.domain.Page;
import sandbox.semo.domain.form.dto.request.CompanyFormDecision;
import sandbox.semo.domain.form.dto.request.CompanyFormRegister;
import sandbox.semo.domain.form.dto.response.CompanyFormInfo;

public interface CompanyFormService {

    void formRegister(CompanyFormRegister companyFormRegister);

    Page<CompanyFormInfo> findAllForms(int page, int size);

    String updateStatus(CompanyFormDecision request);
}
