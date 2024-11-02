package sandbox.semo.application.monitoring.service;

import static sandbox.semo.application.member.exception.MemberErrorCode.MEMBER_NOT_FOUND;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sandbox.semo.application.member.exception.MemberBusinessException;
import sandbox.semo.domain.company.entity.Company;
import sandbox.semo.domain.device.repository.DeviceRepository;
import sandbox.semo.domain.member.entity.Member;
import sandbox.semo.domain.member.repository.MemberRepository;
import sandbox.semo.domain.monitoring.dto.response.DeviceConnectInfo;
import sandbox.semo.domain.monitoring.dto.response.MetricSummary;
import sandbox.semo.domain.monitoring.dto.response.SummaryPageData;
import sandbox.semo.domain.monitoring.dto.response.TotalProcessInfo;

@Log4j2
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MonitoringServiceImpl implements MonitoringService {

    private final MemberRepository memberRepository;
    private final DeviceRepository deviceRepository;

    @Override
    public SummaryPageData fetchSummaryData(Long memberId) {
        Member member = memberRepository.findByIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new MemberBusinessException(MEMBER_NOT_FOUND));
        Company company = member.getCompany();
        List<MetricSummary> metricSummaryData = findMetricSummaryData(company.getId());

        TotalProcessInfo totalProcessInfo = buildTotalProcessInfo(metricSummaryData);
        Map<String, DeviceConnectInfo> allDevices = buildAllDevicesByCompanyId(metricSummaryData);

        return SummaryPageData.builder()
                .companyName(company.getCompanyName())
                .totalProcessInfo(totalProcessInfo)
                .allDevices(allDevices)
                .build();
    }

    private List<MetricSummary> findMetricSummaryData(Long companyId) {
        List<Object[]> response = deviceRepository.findMetricSummaryDataByCompanyId(companyId);
        return response.stream()
                .map(row -> MetricSummary.builder()
                        .deviceAlias((String) row[0])
                        .type((String) row[1])
                        .ip((String) row[2])
                        .port(((Number) row[3]).longValue())
                        .sid((String) row[4])
                        .status((String) row[5])
                        .statusValue(((Number) row[6]).longValue())
                        .lastCollectedAt(
                                row[7] != null ? ((Timestamp) row[7]).toLocalDateTime() : null
                        )
                        .build()
                ).collect(Collectors.toList());
    }

    private TotalProcessInfo buildTotalProcessInfo(List<MetricSummary> data) {
        int activeDeviceCnt = (int) data.stream()
                .filter(this::isActive)
                .count();

        int inActiveDeviceCnt = (int) data.stream()
                .filter(this::isInactive)
                .count();

        int blockedDeviceCnt = (int) data.stream()
                .filter(this::isBlocked)
                .count();

        Map<String, Integer> topUsedDevices = data.stream()
                .filter(this::isActive)
                .sorted((a, b) -> Long.compare(b.getStatusValue(), a.getStatusValue()))
                .limit(3)
                .collect(Collectors.toMap(MetricSummary::getDeviceAlias,
                        m -> m.getStatusValue().intValue(),
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));

        Map<String, Integer> warnDevice = data.stream()
                .filter(this::isBlocked)
                .collect(Collectors.toMap(MetricSummary::getDeviceAlias,
                        m -> m.getStatusValue().intValue(),
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));

        Map<String, Integer> unUsedDevice = data.stream()
                .filter(this::isInactive)
                .collect(Collectors.toMap(MetricSummary::getDeviceAlias,
                        m -> m.getStatusValue().intValue(),
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));

        return TotalProcessInfo.builder()
                .activeDeviceCnt(activeDeviceCnt)
                .inActiveDeviceCnt(inActiveDeviceCnt)
                .blockedDeviceCnt(blockedDeviceCnt)
                .topUsedDevices(topUsedDevices)
                .warnDevice(warnDevice)
                .unUsedDevice(unUsedDevice)
                .build();
    }

    private boolean isActive(MetricSummary metricSummary) {
        return "ACTIVE".equals(metricSummary.getStatus());
    }

    private boolean isInactive(MetricSummary metricSummary) {
        return "INACTIVE".equals(metricSummary.getStatus());
    }

    private boolean isBlocked(MetricSummary metricSummary) {
        return "BLOCKED".equals(metricSummary.getStatus());
    }

    private Map<String, DeviceConnectInfo> buildAllDevicesByCompanyId(List<MetricSummary> data) {
        return data.stream()
                .collect(Collectors.toMap(MetricSummary::getDeviceAlias,
                        m -> DeviceConnectInfo.builder()
                                .type(m.getType())
                                .status(m.getStatus())
                                .sid(m.getSid())
                                .ip(m.getIp())
                                .port(m.getPort())
                                .statusValue(m.getStatusValue())
                                .build())
                );
    }

}
