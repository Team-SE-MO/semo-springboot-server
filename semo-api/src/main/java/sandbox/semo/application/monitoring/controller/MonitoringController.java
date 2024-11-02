package sandbox.semo.application.monitoring.controller;

import static org.springframework.http.HttpStatus.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sandbox.semo.application.common.response.ApiResponse;
import sandbox.semo.application.monitoring.service.MonitoringService;
import sandbox.semo.application.security.authentication.MemberPrincipalDetails;
import sandbox.semo.domain.member.entity.Member;
import sandbox.semo.domain.monitoring.dto.response.SummaryPageData;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/monitoring")
public class MonitoringController {

    private final MonitoringService monitoringService;

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping
    public ApiResponse<SummaryPageData> fetchSummaryInfo(
            @AuthenticationPrincipal MemberPrincipalDetails memberDetails) {
        Member member = memberDetails.getMember();
        SummaryPageData data = monitoringService.fetchSummaryData(member.getId());
        return ApiResponse.successResponse(OK, "성공적으로 장비 요약 정보를 조회 하였습니다.", data);
    }

}
