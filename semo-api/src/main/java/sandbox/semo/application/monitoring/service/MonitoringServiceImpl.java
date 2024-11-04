package sandbox.semo.application.monitoring.service;

import static sandbox.semo.application.member.exception.MemberErrorCode.MEMBER_NOT_FOUND;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.function.Function;
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
import sandbox.semo.domain.monitoring.dto.request.DeviceMonitoring;
import sandbox.semo.domain.monitoring.dto.response.DetailPageData;
import sandbox.semo.domain.monitoring.dto.response.DeviceConnectInfo;
import sandbox.semo.domain.monitoring.dto.response.MetricSummary;
import sandbox.semo.domain.monitoring.dto.response.SummaryPageData;
import sandbox.semo.domain.monitoring.dto.response.TotalProcessInfo;
import sandbox.semo.domain.monitoring.dto.response.TypeData;
import sandbox.semo.domain.monitoring.entity.MonitoringMetric;
import sandbox.semo.domain.monitoring.repository.MetricRepository;

@Log4j2
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MonitoringServiceImpl implements MonitoringService {

    private final MemberRepository memberRepository;
    private final DeviceRepository deviceRepository;
    private final MetricRepository metricRepository;

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
                .sorted((a, b) -> Long.compare(b.getStatusValue(), a.getStatusValue()))
                .limit(3)
                .collect(Collectors.toMap(MetricSummary::getDeviceAlias,
                        m -> m.getStatusValue().intValue(),
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));

        Map<String, Integer> unUsedDevice = data.stream()
                .filter(this::isInactive)
                .filter(m -> m.getStatusValue() >= 4320L) // 3 days == 4320 min
                .sorted((a, b) -> Long.compare(b.getStatusValue(), a.getStatusValue()))
                .limit(3)
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

    @Override
    public DetailPageData fetchDetailData(DeviceMonitoring request, Long companyId) {
        Duration interval = getDurationFromString(request.getInterval());
        String deviceAlias = request.getDeviceAlias();
        Long deviceId = deviceRepository.findIdByAliasAndCompanyId(deviceAlias, companyId);

        List<MonitoringMetric> metrics = metricRepository.findMetricsByTimeRangeAndDeviceId(
                request.getStartTime(),
                request.getEndTime(),
                deviceId
        );

        Function<MonitoringMetric, String> timeStampExtractor = metric -> {
            LocalDateTime collectedAt = metric.getId().getCollectedAt();
            long seconds = interval.getSeconds();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

            return collectedAt.truncatedTo(ChronoUnit.SECONDS)
                    .minusSeconds(collectedAt.getSecond() % seconds)
                    .format(formatter);
        };

        Map<String, Integer> totalSessions = mapMetricValue(metrics, timeStampExtractor,
                MonitoringMetric::getTotalSessionCount);
        Map<String, Integer> activeSessions = mapMetricValue(metrics, timeStampExtractor,
                MonitoringMetric::getActiveSessionCount);
        Map<String, Integer> blockingSessions = mapMetricValue(metrics, timeStampExtractor,
                MonitoringMetric::getBlockingSessionCount);
        Map<String, Integer> waitSessions = mapMetricValue(metrics, timeStampExtractor,
                MonitoringMetric::getWaitSessionCount);

        Map<String, List<TypeData>> sessionCountGroupByUser = mapMetricTypeData(
                metrics,
                timeStampExtractor,
                MonitoringMetric::getSessionCountGroupByUser
        );
        Map<String, List<TypeData>> sessionCountGroupByCommand = mapMetricTypeData(
                metrics,
                timeStampExtractor,
                MonitoringMetric::getSessionCountGroupByCommand
        );
        Map<String, List<TypeData>> sessionCountGroupByMachine = mapMetricTypeData(
                metrics,
                timeStampExtractor,
                MonitoringMetric::getSessionCountGroupByMachine
        );
        Map<String, List<TypeData>> sessionCountGroupByType = mapMetricTypeData(
                metrics,
                timeStampExtractor,
                MonitoringMetric::getSessionCountGroupByType
        );

        return DetailPageData.builder()
                .deviceAlias(deviceAlias)
                .totalSessions(totalSessions)
                .activeSessions(activeSessions)
                .blockingSessions(blockingSessions)
                .waitSessions(waitSessions)
                .sessionCountGroupByUser(sessionCountGroupByUser)
                .sessionCountGroupByCommand(sessionCountGroupByCommand)
                .sessionCountGroupByMachine(sessionCountGroupByMachine)
                .sessionCountGroupByType(sessionCountGroupByType)
                .build();
    }

    private Duration getDurationFromString(String interval) {
        return switch (interval) {
            case "10s" -> Duration.ofSeconds(10);
            case "30s" -> Duration.ofSeconds(30);
            case "1m" -> Duration.ofMinutes(1);
            default -> Duration.ofSeconds(5);
        };
    }

    private <T> Map<String, Integer> mapMetricValue(
            List<MonitoringMetric> metrics,
            Function<MonitoringMetric, String> timeStampExtractor,
            Function<MonitoringMetric, T> valueExtractor) {

        return metrics.stream()
                .collect(Collectors.toMap(
                        timeStampExtractor,
                        metric -> (Integer) valueExtractor.apply(metric),
                        (existing, replacement) -> existing,
                        LinkedHashMap::new));
    }

    private Map<String, List<TypeData>> mapMetricTypeData(
            List<MonitoringMetric> metrics,
            Function<MonitoringMetric, String> timeStampExtractor,
            Function<MonitoringMetric, String> dataExtractor) {

        return metrics.stream()
                .collect(Collectors.toMap(
                        timeStampExtractor,
                        metric -> parseToTypeDataList(dataExtractor.apply(metric)),
                        (existing, replacement) -> existing,
                        LinkedHashMap::new));
    }

    private List<TypeData> parseToTypeDataList(String data) {
        if (data == null || data.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(data.split(","))
                .map(entry -> entry.split(":"))
                .map(parts -> TypeData.builder()
                        .name(parts[0].trim())
                        .value(Integer.parseInt(parts[1].trim()))
                        .build())
                .collect(Collectors.toList());
    }

}
