package sandbox.semo.application.form.service;

import org.springframework.data.domain.Page;
import sandbox.semo.domain.form.dto.request.CompanyFormRegister;
import sandbox.semo.domain.form.dto.request.CompanyFormUpdate;
import sandbox.semo.domain.form.dto.response.CompanyFormList;

public interface CompanyFormService {

    void companyRegister(CompanyFormRegister companyFormRegister);

    Page<CompanyFormList> findAllForms(int page, int size);

    String updateStatus(CompanyFormUpdate request);
}
