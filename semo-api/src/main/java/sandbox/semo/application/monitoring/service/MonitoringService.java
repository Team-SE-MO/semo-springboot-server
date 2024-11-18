package sandbox.semo.application.monitoring.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sandbox.semo.domain.common.dto.response.CursorPage;
import sandbox.semo.domain.common.dto.response.OffsetPage;
import sandbox.semo.domain.monitoring.dto.request.DeviceMonitoring;
import sandbox.semo.domain.monitoring.dto.response.DailyJobExecutionInfo;
import sandbox.semo.domain.monitoring.dto.response.DetailPageData;
import sandbox.semo.domain.monitoring.dto.response.SessionDataGrid;
import sandbox.semo.domain.monitoring.dto.response.SessionDataInfo;
import sandbox.semo.domain.monitoring.dto.response.SummaryPageData;
import sandbox.semo.domain.monitoring.dto.response.StepInfo;
import sandbox.semo.domain.monitoring.dto.response.MetaExecutionData;

public interface MonitoringService {

    SummaryPageData fetchSummaryData(Long memberId);

    DetailPageData fetchDetailData(DeviceMonitoring request, Long companyId);

    Page<SessionDataGrid> getPaginated(Pageable pageable);

    MetaExecutionData getRealTimeJobExecutionTimes();

    DailyJobExecutionInfo getDailyJobExecutionTimes();

    StepInfo getStepExecutionData();

    CursorPage<SessionDataInfo> fetchSessionData(
            String deviceAlias, Long companyId, String collectedAt
    );

    OffsetPage<SessionDataInfo> fetchSessionDataWithinTimeRange(
            String deviceAlias, Long companyId, String startTime, int page
    );

}
