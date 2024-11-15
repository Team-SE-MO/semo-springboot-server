package sandbox.semo.application.file.controller;

import static org.springframework.http.HttpStatus.OK;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sandbox.semo.application.common.response.ApiResponse;
import sandbox.semo.application.file.service.FileService;
import sandbox.semo.application.security.authentication.JwtMemberDetails;
import sandbox.semo.domain.file.dto.CsvFileInfo;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/file")
public class FileController {

    private final FileService fileService;
    //TODO : 현재 테스트용으로 SUPER 권한으로 조회했는데 수정할 예정입니다.
    @PreAuthorize("hasAnyRole('SUPER','ADMIN','USER')")
    @GetMapping
    public ApiResponse<List<CsvFileInfo>> getCsvFileListByCompany(
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
        @AuthenticationPrincipal JwtMemberDetails memberDetails
    ) {
        List<CsvFileInfo> data = fileService.getCsvFileListByCompany(memberDetails.getCompanyId(),
            date);
        return ApiResponse.successResponse(OK, "성공적으로 DEVICE가 조회 되었습니다.", data);
    }

    @PreAuthorize("hasAnyRole('SUPER','ADMIN','USER')")
    @GetMapping("/download")
    public ResponseEntity<Resource> downloadCsvFile(
        @RequestParam String key,
        @RequestParam(required = false) Long deviceId,
        @AuthenticationPrincipal JwtMemberDetails memberDetails
    ) {
        return fileService.downloadCsvFile(key, memberDetails.getCompanyId(),deviceId);
    }
}