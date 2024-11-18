package sandbox.semo.application.monitoring.controller;

import static org.springframework.http.HttpStatus.OK;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sandbox.semo.application.common.response.ApiResponse;
import sandbox.semo.application.monitoring.service.MonitoringService;
import sandbox.semo.application.security.authentication.JwtMemberDetails;
import sandbox.semo.domain.common.dto.response.CursorPage;
import sandbox.semo.domain.common.dto.response.OffsetPage;
import sandbox.semo.domain.monitoring.dto.request.DeviceMonitoring;
import sandbox.semo.domain.monitoring.dto.response.DailyJobExecutionInfo;
import sandbox.semo.domain.monitoring.dto.response.DetailPageData;
import sandbox.semo.domain.monitoring.dto.response.MetaExecutionData;
import sandbox.semo.domain.monitoring.dto.response.SessionDataGrid;
import sandbox.semo.domain.monitoring.dto.response.SessionDataInfo;
import sandbox.semo.domain.monitoring.dto.response.StepInfo;
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
            @AuthenticationPrincipal JwtMemberDetails memberDetails) {
        SummaryPageData data = monitoringService.fetchSummaryData(memberDetails.getId());
        return ApiResponse.successResponse(OK, "성공적으로 장비 요약 정보를 조회 하였습니다.", data);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/chart")
    public ApiResponse<DetailPageData> fetchDetailInfo(
            @RequestBody DeviceMonitoring request,
            @AuthenticationPrincipal JwtMemberDetails memberDetails
    ) {
        Long companyId = memberDetails.getCompanyId();
        DetailPageData data = monitoringService.fetchDetailData(request, companyId);
        return ApiResponse.successResponse(OK, "성공적으로 장비 차트 정보를 조회 하였습니다.", data);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/grid")
    public ApiResponse<Page<SessionDataGrid>> getPaginated(
            @AuthenticationPrincipal JwtMemberDetails memberDetails,
            @RequestParam int page,
            @RequestParam int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SessionDataGrid> data = monitoringService.getPaginated(pageable);
        return ApiResponse.successResponse(OK, "성공적으로 세션 데이터 그리드를 조회하였습니다.", data);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/grid")
    public ApiResponse<CursorPage<SessionDataInfo>> fetchGridInfo(
            @RequestParam String deviceAlias,
            @RequestParam String collectedAt,
            @AuthenticationPrincipal JwtMemberDetails memberDetails
    ) {
        Long companyId = memberDetails.getCompanyId();
        CursorPage<SessionDataInfo> data = monitoringService.fetchSessionData(deviceAlias,
                companyId, collectedAt);
        return ApiResponse.successResponse(OK, "성공적으로 장비 차트 정보를 조회 하였습니다.", data);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/grid/search")
    public ApiResponse<OffsetPage<SessionDataInfo>> fetchGridInfoWithInTimeRange(
            @RequestParam String deviceAlias,
            @RequestParam String startTime,
            @RequestParam(defaultValue = "1") int page,
            @AuthenticationPrincipal JwtMemberDetails memberDetails
    ) {
        Long companyId = memberDetails.getCompanyId();
        OffsetPage<SessionDataInfo> data = monitoringService.fetchSessionDataWithinTimeRange(
                deviceAlias, companyId, startTime, page
        );
        return ApiResponse.successResponse(OK, "성공적으로 장비 차트 정보를 조회 하였습니다.", data);
    }

    @GetMapping("/batch-chart")
    public ApiResponse<MetaExecutionData> getRealTimeJobExecutionTimes() {
        MetaExecutionData data = monitoringService.getRealTimeJobExecutionTimes();
        return ApiResponse.successResponse(OK, "성공적으로 현재 세션 Job의 실행시간을 조회했습니다.", data);
    }

    @GetMapping("/batch-chart/daily")
    public ApiResponse<DailyJobExecutionInfo> getDailyJobExecutionTimes() {
        DailyJobExecutionInfo data = monitoringService.getDailyJobExecutionTimes();
        return ApiResponse.successResponse(OK, "성공적으로 일별 Job의 실행시간을 조회했습니다.", data);
    }

    @GetMapping("/failure")
    public ApiResponse<StepInfo> getStepErrorStatistics() {
        StepInfo data = monitoringService.getStepExecutionData();
        return ApiResponse.successResponse(OK, "성공적으로 배치 Step의 에러 통계를 조회했습니다.", data);
    }

}
