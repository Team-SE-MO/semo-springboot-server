//package sandbox.semo.batch.service.step;
//
//import jakarta.annotation.PreDestroy;
//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.log4j.Log4j2;
//import org.springframework.batch.item.Chunk;
//import org.springframework.batch.item.ItemWriter;
//import sandbox.semo.domain.monitoring.entity.SessionData;
//
//@Log4j2
//@RequiredArgsConstructor
//public class CompanyPartitionedFileWriter implements ItemWriter<SessionData> {
//
//    private final String baseBackupPath;
//    private static final int BUFFER_SIZE = 8192 * 4;
//    private final Map<Long, BufferedWriter> companyWriters = new ConcurrentHashMap<>();
//    private final Map<Long, StringBuilder> stringBuilders = new ConcurrentHashMap<>();
//
//    @Override
//    public void write(Chunk<? extends SessionData> chunk) throws Exception {
//        int chunkSize = chunk.size();
//        for (SessionData data : chunk) {
//            Long companyId = data.getDevice().getCompany().getId();
//            BufferedWriter writer = companyWriters.computeIfAbsent(companyId, this::createWriter);
//            writeDataToCsv(writer, data);
//        }
//        // chunk 단위로 모든 writer flush
//        for (BufferedWriter writer : companyWriters.values()) {
//            writer.flush();
//        }
//        log.info("write complete.{}개 ", chunk.size());
//    }
//
//    private BufferedWriter createWriter(Long companyId) {
//        try {
//            String fileName = String.format("%s/company_%d_%s.csv",
//                baseBackupPath,
//                companyId,
//                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
//
//            File backupFile = new File(fileName);
//            File backupDir = backupFile.getParentFile();
//
//            if (!backupDir.exists() && !backupDir.mkdirs()) {
//                throw new RuntimeException("백업 디렉토리 생성 실패: " + backupDir);
//            }
//
//            log.info(">>> [ 📁 회사별 백업 파일 생성 시작: {} ]", fileName);
//            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
//            writeHeader(writer);
//            return writer;
//        } catch (IOException e) {
//            throw new RuntimeException("Failed to create writer for company: " + companyId, e);
//        }
//    }
//
//    private void writeHeader(BufferedWriter writer) throws IOException {
//        writer.write("DEVICE_ID,SID,COLLECTED_AT,SERIAL,USERNAME,COMMAND,COMMAND_NAME," +
//            "STATUS,SCHEMA_NAME,OS_USER,PROCESS,MACHINE,PORT,TERMINAL,PROGRAM,TYPE," +
//            "SQL_ID,SQL_EXEC_START,SQL_EXEC_ID,SQL_TEXT,MODULE,ACTION,LOGON_TIME," +
//            "LAST_CALL_ET,FAILED_OVER,BLOCKING_SESSION_STATUS,EVENT,WAIT_CLASS," +
//            "STATE,WAIT_TIME_MICRO,TIME_REMAINING_MICRO,SERVICE_NAME\n");
//        writer.flush();
//    }
//
//    private void writeDataToCsv(BufferedWriter writer, SessionData sessionData) throws IOException {
//        writer.write(formatCsvLine(sessionData));
//    }
//
//    private String formatCsvLine(SessionData sessionData) {
//        return String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
//            sessionData.getId().getDeviceId(),
//            escape(String.valueOf(sessionData.getId().getSid())),
//            sessionData.getId().getCollectedAt(),
//            nullSafe(sessionData.getSerial()),
//            escape(sessionData.getUsername()),
//            nullSafe(sessionData.getCommand()),
//            escape(sessionData.getCommandName()),
//            escape(sessionData.getStatus()),
//            escape(sessionData.getSchemaName()),
//            escape(sessionData.getOsUser()),
//            escape(sessionData.getProcess()),
//            escape(sessionData.getMachine()),
//            nullSafe(sessionData.getPort()),
//            escape(sessionData.getTerminal()),
//            escape(sessionData.getProgram()),
//            escape(sessionData.getType()),
//            escape(sessionData.getSqlId()),
//            sessionData.getSqlExecStart() != null ? sessionData.getSqlExecStart().toString() : "",
//            nullSafe(sessionData.getSqlExecId()),
//            escape(sessionData.getSqlText()),
//            escape(sessionData.getModule()),
//            escape(sessionData.getAction()),
//            sessionData.getLogonTime() != null ? sessionData.getLogonTime().toString() : "",
//            nullSafe(sessionData.getLastCallEt()),
//            escape(sessionData.getFailedOver()),
//            escape(sessionData.getBlockingSessionStatus()),
//            escape(sessionData.getEvent()),
//            escape(sessionData.getWaitClass()),
//            escape(sessionData.getState()),
//            nullSafe(sessionData.getWaitTimeMicro()),
//            nullSafe(sessionData.getTimeRemainingMicro()),
//            escape(sessionData.getServiceName())
//        );
//    }
//
//    private String nullSafe(Long value) {
//        return value != null ? value.toString() : "";
//    }
//
//    private String nullSafe(Integer value) {
//        return value != null ? value.toString() : "";
//    }
//
//    private String escape(String value) {
//        if (value == null) {
//            return "";
//        }
//        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
//            return "\"" + value.replace("\"", "\"\"") + "\"";
//        }
//        return value;
//    }
//
//    @PreDestroy
//    public void cleanup() {
//        for (Map.Entry<Long, BufferedWriter> entry : companyWriters.entrySet()) {
//            try {
//                BufferedWriter writer = entry.getValue();
//                writer.flush();
//                writer.close();
//                log.info(">>> [ ✅ 회사별({}) 백업 파일 작성 완료 ]", entry.getKey());
//            } catch (IOException e) {
//                log.error("CSV 파일 닫기 실패 (Company ID: {}): {}", entry.getKey(), e.getMessage(), e);
//            }
//        }
//    }
//}

package sandbox.semo.batch.service.step;

import jakarta.annotation.PreDestroy;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import sandbox.semo.domain.monitoring.dto.request.CsvFileData;

@Log4j2
@RequiredArgsConstructor
public class CompanyPartitionedFileWriter implements ItemWriter<CsvFileData> {

    private final String baseBackupPath;
    private static final int BUFFER_SIZE = 8192 * 64;
    private final Map<Long, BufferedWriter> companyWriters = new ConcurrentHashMap<>();
    private int totalWriteCount = 0;

    @Override
    public void write(Chunk<? extends CsvFileData> chunk) throws Exception {
        long startTime = System.currentTimeMillis();
        log.info(">>> 청크 데이터 크기: {}", chunk.size());

        // 회사별 그룹화 (이미 정렬되어 있으므로 더 효율적)
        long groupingStart = System.currentTimeMillis();
        Map<Long, List<CsvFileData>> companyGroups = chunk.getItems().stream()
            .collect(Collectors.groupingBy(CsvFileData::getCompanyId));  // 직접 companyId 접근
        long groupingEnd = System.currentTimeMillis();

        log.info(">>> 그룹화 소요시간: {}ms, 회사 수: {}",
            groupingEnd - groupingStart, companyGroups.size());

        // 파일 쓰기
        for (Map.Entry<Long, List<CsvFileData>> entry : companyGroups.entrySet()) {
            Long companyId = entry.getKey();
            List<CsvFileData> items = entry.getValue();

            StringBuilder builder = new StringBuilder(items.size() * 500);
            for (CsvFileData data : items) {
                appendCsvLine(builder, data);
            }

            BufferedWriter writer = companyWriters.computeIfAbsent(companyId, this::createWriter);
            writer.write(builder.toString());
        }

        totalWriteCount += chunk.size();
        long endTime = System.currentTimeMillis();
        log.info(">>> 전체 처리 소요시간: {}ms (그룹화: {}ms, 파일쓰기: {}ms)",
            endTime - startTime, groupingEnd - groupingStart, endTime - groupingEnd);
    }

    private void appendCsvLine(StringBuilder builder, CsvFileData data) {
        builder.append(data.getDeviceId()).append(',')
            .append(escape(String.valueOf(data.getSid()))).append(',')
            .append(data.getCollectedAt()).append(',')
            .append(nullSafe(data.getSerial())).append(',')
            .append(escape(data.getUsername())).append(',')
            .append(nullSafe(data.getCommand())).append(',')
            .append(escape(data.getCommandName())).append(',')
            .append(escape(data.getStatus())).append(',')
            .append(escape(data.getSchemaName())).append(',')
            .append(escape(data.getOsUser())).append(',')
            .append(escape(data.getProcess())).append(',')
            .append(escape(data.getMachine())).append(',')
            .append(nullSafe(data.getPort())).append(',')
            .append(escape(data.getTerminal())).append(',')
            .append(escape(data.getProgram())).append(',')
            .append(escape(data.getType())).append(',')
            .append(escape(data.getSqlId())).append(',')
            .append(data.getSqlExecStart() != null ? data.getSqlExecStart() : "").append(',')
            .append(nullSafe(data.getSqlExecId())).append(',')
            .append(escape(data.getSqlText())).append(',')
            .append(escape(data.getModule())).append(',')
            .append(escape(data.getAction())).append(',')
            .append(data.getLogonTime() != null ? data.getLogonTime() : "").append(',')
            .append(nullSafe(data.getLastCallEt())).append(',')
            .append(escape(data.getFailedOver())).append(',')
            .append(escape(data.getBlockingSessionStatus())).append(',')
            .append(escape(data.getEvent())).append(',')
            .append(escape(data.getWaitClass())).append(',')
            .append(escape(data.getState())).append(',')
            .append(nullSafe(data.getWaitTimeMicro())).append(',')
            .append(nullSafe(data.getTimeRemainingMicro())).append(',')
            .append(escape(data.getServiceName())).append('\n');
    }

    private BufferedWriter createWriter(Long companyId) {
        try {
            String fileName = String.format("%s/company_%d_%s.csv",
                baseBackupPath,
                companyId,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));

            File backupFile = new File(fileName);
            File backupDir = backupFile.getParentFile();

            if (!backupDir.exists() && !backupDir.mkdirs()) {
                throw new RuntimeException("백업 디렉토리 생성 실패: " + backupDir);
            }

            log.info(">>> [ 📁 회사별 백업 파일 생성 시작: {} ]", fileName);

            BufferedWriter writer = Files.newBufferedWriter(
                backupFile.toPath(),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.APPEND
            );

            writeHeader(writer);
            return writer;
        } catch (IOException e) {
            throw new RuntimeException("Failed to create writer for company: " + companyId, e);
        }
    }

    private void writeHeader(BufferedWriter writer) throws IOException {
        writer.write("DEVICE_ID,SID,COLLECTED_AT,SERIAL,USERNAME,COMMAND,COMMAND_NAME," +
            "STATUS,SCHEMA_NAME,OS_USER,PROCESS,MACHINE,PORT,TERMINAL,PROGRAM,TYPE," +
            "SQL_ID,SQL_EXEC_START,SQL_EXEC_ID,SQL_TEXT,MODULE,ACTION,LOGON_TIME," +
            "LAST_CALL_ET,FAILED_OVER,BLOCKING_SESSION_STATUS,EVENT,WAIT_CLASS," +
            "STATE,WAIT_TIME_MICRO,TIME_REMAINING_MICRO,SERVICE_NAME\n");
    }

    private String nullSafe(Long value) {
        return value != null ? value.toString() : "";
    }

    private String nullSafe(Integer value) {
        return value != null ? value.toString() : "";
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    @PreDestroy
    public void cleanup() {
        for (Map.Entry<Long, BufferedWriter> entry : companyWriters.entrySet()) {
            try {
                BufferedWriter writer = entry.getValue();
                writer.flush();
                writer.close();
                log.info(">>> [ ✅ 회사({}) 백업 파일 작성 완료 - 처리 건수: {} ]",
                    entry.getKey(), totalWriteCount);
            } catch (IOException e) {
                log.error("파일 닫기 실패 (Company ID: {})", entry.getKey(), e);
            }
        }
    }
}
