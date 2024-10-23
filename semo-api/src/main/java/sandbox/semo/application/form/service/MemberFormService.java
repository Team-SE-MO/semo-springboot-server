package sandbox.semo.application.form.service;

import org.springframework.data.domain.Page;
import sandbox.semo.domain.form.dto.request.MemberFormDecision;
import sandbox.semo.domain.form.dto.request.MemberFormRegister;
import sandbox.semo.domain.form.dto.response.MemberFormList;

public interface MemberFormService {

    void formRegister(MemberFormRegister request);

    Page<MemberFormList> findAllForms(int page, int size);

    String updateForm(MemberFormDecision request);
}
