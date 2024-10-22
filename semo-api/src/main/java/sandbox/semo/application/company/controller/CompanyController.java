package sandbox.semo.application.company.controller;

import static org.springframework.http.HttpStatus.OK;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sandbox.semo.application.common.response.ApiResponse;
import sandbox.semo.application.company.service.CompanyService;


@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/company")
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping("/{id}")
    public ApiResponse<Long> companyRegister(@PathVariable(value = "id") Long formId) {
        Long data = companyService.companyRegister(formId);
        return ApiResponse.successResponse(
                OK,
                "성공적으로 회사가 등록되었습니다.",
                data
        );
    }
}
