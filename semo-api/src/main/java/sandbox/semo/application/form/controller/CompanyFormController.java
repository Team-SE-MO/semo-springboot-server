package sandbox.semo.application.form.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sandbox.semo.application.common.response.ApiResponse;
import sandbox.semo.application.form.service.CompanyFormService;
import sandbox.semo.domain.form.dto.request.CompanyFormRegister;
import sandbox.semo.domain.form.dto.request.CompanyFormUpdate;
import sandbox.semo.domain.form.dto.response.CompanyFormList;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/form/company")
public class CompanyFormController {

    private final CompanyFormService companyFormService;

    @PostMapping
    public ResponseEntity<ApiResponse> formRegister(
            @RequestBody @Valid CompanyFormRegister registerForm) {
        companyFormService.formRegister(registerForm);
        return ResponseEntity.ok().body(
                ApiResponse.successResponse(
                        HttpStatus.OK,
                        "성공적으로 폼을 제출하였습니다."
                )
        );
    }

    @Secured("SUPER")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<CompanyFormList>>> registerList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<CompanyFormList> companyFormLists = companyFormService.findAllForms(page, size);
        return ResponseEntity.ok().body(
                ApiResponse.successResponse(
                        HttpStatus.OK,
                        "성공적으로 목록을 조회하였습니다.",
                        companyFormLists
                )
        );

    }

    @Secured("SUPER")
    @PatchMapping
    public ResponseEntity<ApiResponse> formUpdate(
            @RequestBody @Valid CompanyFormUpdate updateForm) {
        String response = companyFormService.updateStatus(updateForm);

        return ResponseEntity.ok().body(
                ApiResponse.successResponse(
                        HttpStatus.OK,
                        "성공적으로 처리되었습니다.",
                        response
                )
        );
    }

}