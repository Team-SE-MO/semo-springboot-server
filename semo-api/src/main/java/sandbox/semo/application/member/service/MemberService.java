package sandbox.semo.application.member.service;

import sandbox.semo.domain.member.dto.request.MemberRegister;

public interface MemberService {

    void register(MemberRegister request);

}
