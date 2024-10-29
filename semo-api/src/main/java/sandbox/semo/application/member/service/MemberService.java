package sandbox.semo.application.member.service;

import org.springframework.data.domain.Page;
import sandbox.semo.domain.member.dto.request.MemberFormDecision;
import sandbox.semo.domain.member.dto.request.MemberFormRegister;
import sandbox.semo.domain.member.dto.request.MemberRegister;
import sandbox.semo.domain.member.dto.response.MemberFormInfo;
import sandbox.semo.domain.member.entity.Role;

public interface MemberService {

    String register(MemberRegister request, Role role);

    void formRegister(MemberFormRegister request);

    Page<MemberFormInfo> findAllForms(int page, int size);

    String updateForm(MemberFormDecision request);

    Boolean checkEmailDuplicate(String email);

    void updatePassword(Long memberId, String newPassword);

    void deleteMember(Role memberRole, String loginId);
}
