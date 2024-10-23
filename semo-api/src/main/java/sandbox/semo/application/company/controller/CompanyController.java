package sandbox.semo.application.company.controller;

import static org.springframework.http.HttpStatus.OK;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sandbox.semo.application.common.response.ApiResponse;
import sandbox.semo.application.company.service.CompanyService;
import sandbox.semo.domain.company.dto.request.CompanyFormDecision;
import sandbox.semo.domain.company.dto.request.CompanyFormRegister;
import sandbox.semo.domain.company.dto.response.CompanyFormInfo;
import sandbox.semo.domain.company.entity.Company;


@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/company")
public class CompanyController {

    private final CompanyService companyService;

    @PreAuthorize("hasRole('SUPER')")
    @PostMapping("/{id}")
    public ApiResponse<Long> companyRegister(@PathVariable(value = "id") Long formId) {
        Long data = companyService.companyRegister(formId);
        return ApiResponse.successResponse(
                OK,
                "성공적으로 회사가 등록되었습니다.",
                data
        );
    }

    @GetMapping
    public ApiResponse<List<Company>> companyList(@RequestParam(required = false) String keyword) {
        List<Company> data = companyService.searchCompanyByName(keyword);
        return ApiResponse.successResponse(
                OK,
                "성공적으로 회사 목록을 조회하였습니다.",
                data
        );
    }

    @PostMapping("/form")
    public ApiResponse<Void> formRegister(
            @RequestBody @Valid CompanyFormRegister registerForm) {
        companyService.formRegister(registerForm);
        return ApiResponse.successResponse(
                OK,
                "성공적으로 폼을 제출하였습니다.");
    }

    @PreAuthorize("hasRole('SUPER')")
    @GetMapping("/form")
    public ApiResponse<Page<CompanyFormInfo>> registerList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<CompanyFormInfo> companyFormLists = companyService.findAllForms(page, size);
        return ApiResponse.successResponse(
                OK,
                "성공적으로 목록을 조회하였습니다.",
                companyFormLists
        );

    }

    @PreAuthorize("hasRole('SUPER')")
    @PatchMapping("/form")
    public ApiResponse<String> formUpdate(
            @RequestBody @Valid CompanyFormDecision updateForm) {
        String data = companyService.updateStatus(updateForm);

        return ApiResponse.successResponse(
                OK,
                "성공적으로 처리되었습니다.",
                data
        );
    }
}
