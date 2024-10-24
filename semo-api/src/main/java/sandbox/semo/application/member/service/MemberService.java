package sandbox.semo.application.member.service;

import org.springframework.data.domain.Page;
import sandbox.semo.domain.member.dto.request.MemberFormDecision;
import sandbox.semo.domain.member.dto.request.MemberFormRegister;
import sandbox.semo.domain.member.dto.request.MemberRegister;
import sandbox.semo.domain.member.dto.response.MemberFormInfo;

public interface MemberService {

    void register(MemberRegister request);

    void formRegister(MemberFormRegister request);

    Page<MemberFormInfo> findAllForms(int page, int size);

    String updateForm(MemberFormDecision request);

    Boolean checkEmail(String email);
}
