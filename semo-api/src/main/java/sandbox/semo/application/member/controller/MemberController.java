package sandbox.semo.application.member.controller;

import static org.springframework.http.HttpStatus.OK;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sandbox.semo.application.common.response.ApiResponse;
import sandbox.semo.application.member.service.MemberService;
import sandbox.semo.domain.member.dto.request.MemberFormDecision;
import sandbox.semo.domain.member.dto.request.MemberFormRegister;
import sandbox.semo.domain.member.dto.request.MemberRegister;
import sandbox.semo.domain.member.dto.response.MemberFormInfo;

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


    @PostMapping("/form")
    public ApiResponse<Void> formRegister(@RequestBody @Valid MemberFormRegister registerForm) {
        memberService.formRegister(registerForm);
        return ApiResponse.successResponse(
                OK,
                "성공적으로 폼을 제출하였습니다.");
    }

    @PreAuthorize("hasRole('SUPER')")
    @GetMapping("/form")
    public ApiResponse<Page<MemberFormInfo>> getFormList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<MemberFormInfo> data = memberService.findAllForms(page, size);
        return ApiResponse.successResponse(
                OK,
                "성공적으로 목록을 조회하였습니다.",
                data
        );

    }

    @PreAuthorize("hasRole('SUPER')")
    @PatchMapping("/form")
    public ApiResponse<String> formUpdate(@RequestBody @Valid MemberFormDecision formDecision) {
        String data = memberService.updateForm(formDecision);
        return ApiResponse.successResponse(
                OK,
                "성공적으로 처리되었습니다.",
                data);
    }
}
