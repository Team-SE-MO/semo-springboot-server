package sandbox.semo.application.form.controller;

import static org.springframework.http.HttpStatus.OK;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sandbox.semo.application.common.response.ApiResponse;
import sandbox.semo.application.form.service.MemberFormService;
import sandbox.semo.domain.form.dto.request.MemberFormRegister;
import sandbox.semo.domain.form.dto.response.MemberFormList;

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

    @PreAuthorize("hasRole('SUPER')")
    @GetMapping
    public ApiResponse<Page<MemberFormList>> formList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<MemberFormList> data = memberFormService.findAllForms(page, size);
        return ApiResponse.successResponse(
                OK,
                "성공적으로 목록을 조회하였습니다.",
                data
        );

    }

}
