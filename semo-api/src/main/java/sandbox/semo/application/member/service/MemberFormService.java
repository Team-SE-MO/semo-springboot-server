package sandbox.semo.application.member.service;

import org.springframework.data.domain.Page;
import sandbox.semo.domain.member.dto.request.MemberFormDecision;
import sandbox.semo.domain.member.dto.request.MemberFormRegister;
import sandbox.semo.domain.member.dto.response.MemberFormInfo;

public interface MemberFormService {

    void formRegister(MemberFormRegister request);

    Page<MemberFormInfo> findAllForms(int page, int size);

    String updateForm(MemberFormDecision request);
}
