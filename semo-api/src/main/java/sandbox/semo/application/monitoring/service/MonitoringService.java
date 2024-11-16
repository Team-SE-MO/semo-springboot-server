package sandbox.semo.application.monitoring.service;


import sandbox.semo.domain.monitoring.dto.request.DeviceMonitoring;
import sandbox.semo.domain.monitoring.dto.response.DailyJobExecutionInfo;
import sandbox.semo.domain.monitoring.dto.response.DetailPageData;
import sandbox.semo.domain.monitoring.dto.response.MetaExecutionData;
import sandbox.semo.domain.monitoring.dto.response.StepInfo;
import sandbox.semo.domain.monitoring.dto.response.SummaryPageData;

public interface MonitoringService {

    SummaryPageData fetchSummaryData(Long memberId);

    DetailPageData fetchDetailData(DeviceMonitoring request, Long companyId);

    MetaExecutionData getRealTimeJobExecutionTimes();

    DailyJobExecutionInfo getDailyJobExecutionTimes();

    StepInfo getStepExecutionData();
}
