package sandbox.semo.application.form.controller;

import static org.springframework.http.HttpStatus.OK;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sandbox.semo.application.common.response.ApiResponse;
import sandbox.semo.application.form.service.MemberFormService;
import sandbox.semo.domain.form.dto.request.MemberFormRegister;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/form/member")
public class MemberFormController {

    private final MemberFormService memberFormService;

    @PostMapping
    public ApiResponse<Void> formRegister(@RequestBody @Valid MemberFormRegister registerForm) {
        memberFormService.formRegister(registerForm);
        return ApiResponse.successResponse(
                OK,
                "성공적으로 폼을 제출하였습니다.");
    }

}
