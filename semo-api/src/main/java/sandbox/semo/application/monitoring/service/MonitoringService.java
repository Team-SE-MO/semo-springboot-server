package sandbox.semo.application.monitoring.service;

import sandbox.semo.domain.common.dto.response.CursorPage;
import sandbox.semo.domain.monitoring.dto.request.DeviceMonitoring;
import sandbox.semo.domain.monitoring.dto.response.DetailPageData;
import sandbox.semo.domain.monitoring.dto.response.SessionDataInfo;
import sandbox.semo.domain.monitoring.dto.response.SummaryPageData;

public interface MonitoringService {

    SummaryPageData fetchSummaryData(Long memberId);

    DetailPageData fetchDetailData(DeviceMonitoring request, Long companyId);

    CursorPage<SessionDataInfo> fetchSessionData(String deviceAlias, Long companyId, String collectedAt);

}
