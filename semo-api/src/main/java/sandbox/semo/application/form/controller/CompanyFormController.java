package sandbox.semo.application.form.controller;

import static org.springframework.http.HttpStatus.OK;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sandbox.semo.application.common.response.ApiResponse;
import sandbox.semo.application.form.service.CompanyFormService;
import sandbox.semo.domain.form.dto.request.CompanyFormRegister;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/form/company")
public class CompanyFormController {

    private final CompanyFormService companyFormService;

    @PostMapping
    public ResponseEntity<ApiResponse> formRegister(@RequestBody CompanyFormRegister registerForm) {
        companyFormService.companyRegister(registerForm);
        return ResponseEntity.ok().body(
                ApiResponse.successResponse(
                        OK,
                        "성공적으로 폼을 제출하였습니다."
                )
        );
    }

}