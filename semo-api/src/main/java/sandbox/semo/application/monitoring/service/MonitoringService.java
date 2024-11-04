package sandbox.semo.application.monitoring.service;

import sandbox.semo.domain.monitoring.dto.response.SummaryPageData;

public interface MonitoringService {

    SummaryPageData fetchSummaryData(Long memberId);

}
