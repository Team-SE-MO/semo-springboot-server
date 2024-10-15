package sandbox.semo.member.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sandbox.semo.member.dto.request.MemberRegister;
import sandbox.semo.member.service.MemberService;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/member")
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    public ResponseEntity<?> register(@RequestBody MemberRegister memberRegister) {
        memberService.register(memberRegister);
        return ResponseEntity.ok()
                .body("성공적으로 계정 생성이 완료 되었습니다.");
    }

}
