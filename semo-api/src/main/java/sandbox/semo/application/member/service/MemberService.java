package sandbox.semo.application.member.service;

import java.util.List;
import org.springframework.data.domain.Page;
import sandbox.semo.domain.common.dto.response.FormDecisionResponse;
import sandbox.semo.domain.member.dto.request.MemberFormDecision;
import sandbox.semo.domain.member.dto.request.MemberFormRegister;
import sandbox.semo.domain.member.dto.request.MemberRegister;
import sandbox.semo.domain.member.dto.request.MemberRemove;
import sandbox.semo.domain.member.dto.request.MemberSearchFilter;
import sandbox.semo.domain.member.dto.request.SuperRegister;
import sandbox.semo.domain.member.dto.request.UpdatePassword;
import sandbox.semo.domain.member.dto.response.MemberFormInfo;
import sandbox.semo.domain.member.dto.response.MemberInfo;
import sandbox.semo.domain.member.entity.Role;

public interface MemberService {

    void superRegister(SuperRegister request);

    String register(MemberRegister request, Role role);

    void formRegister(MemberFormRegister request);

    Page<MemberFormInfo> findAllForms(int page, int size);

    FormDecisionResponse updateForm(MemberFormDecision request);

    Boolean checkEmailDuplicate(String email);

    void updatePassword(UpdatePassword request);

    void deleteMember(MemberRemove request);

    List<MemberInfo> findAllMembers(Long ownCompanyId, Role ownRole, MemberSearchFilter request);
}
