package sandbox.semo.application.monitoring.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sandbox.semo.domain.monitoring.dto.request.DeviceMonitoring;
import sandbox.semo.domain.monitoring.dto.response.DetailPageData;
import sandbox.semo.domain.monitoring.dto.response.SessionDataGrid;
import sandbox.semo.domain.monitoring.dto.response.SummaryPageData;

public interface MonitoringService {

    SummaryPageData fetchSummaryData(Long memberId);

    DetailPageData fetchDetailData(DeviceMonitoring request, Long companyId);

    Page<SessionDataGrid> getPaginated(Pageable pageable);

}
