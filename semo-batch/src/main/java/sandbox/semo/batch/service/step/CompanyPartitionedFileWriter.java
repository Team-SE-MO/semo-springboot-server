package sandbox.semo.batch.service.step;

import jakarta.annotation.PreDestroy;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import sandbox.semo.domain.monitoring.entity.SessionData;

@Log4j2
@RequiredArgsConstructor
public class CompanyPartitionedFileWriter implements ItemWriter<SessionData> {

    private final String baseBackupPath;
    private final Map<Long, BufferedWriter> companyWriters = new ConcurrentHashMap<>();

    @Override
    public void write(Chunk<? extends SessionData> chunk) throws Exception {
        for (SessionData data : chunk) {
            Long companyId = data.getDevice().getCompany().getId();
            BufferedWriter writer = companyWriters.computeIfAbsent(companyId, this::createWriter);
            writeDataToCsv(writer, data);
        }
        // chunk 단위로 모든 writer flush
        for (BufferedWriter writer : companyWriters.values()) {
            writer.flush();
        }
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
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
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
        writer.flush();
    }

    private void writeDataToCsv(BufferedWriter writer, SessionData sessionData) throws IOException {
        writer.write(formatCsvLine(sessionData));
    }

    private String formatCsvLine(SessionData sessionData) {
        return String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
            sessionData.getId().getDeviceId(),
            escape(String.valueOf(sessionData.getId().getSid())),
            sessionData.getId().getCollectedAt(),
            nullSafe(sessionData.getSerial()),
            escape(sessionData.getUsername()),
            nullSafe(sessionData.getCommand()),
            escape(sessionData.getCommandName()),
            escape(sessionData.getStatus()),
            escape(sessionData.getSchemaName()),
            escape(sessionData.getOsUser()),
            escape(sessionData.getProcess()),
            escape(sessionData.getMachine()),
            nullSafe(sessionData.getPort()),
            escape(sessionData.getTerminal()),
            escape(sessionData.getProgram()),
            escape(sessionData.getType()),
            escape(sessionData.getSqlId()),
            sessionData.getSqlExecStart() != null ? sessionData.getSqlExecStart().toString() : "",
            nullSafe(sessionData.getSqlExecId()),
            escape(sessionData.getSqlText()),
            escape(sessionData.getModule()),
            escape(sessionData.getAction()),
            sessionData.getLogonTime() != null ? sessionData.getLogonTime().toString() : "",
            nullSafe(sessionData.getLastCallEt()),
            escape(sessionData.getFailedOver()),
            escape(sessionData.getBlockingSessionStatus()),
            escape(sessionData.getEvent()),
            escape(sessionData.getWaitClass()),
            escape(sessionData.getState()),
            nullSafe(sessionData.getWaitTimeMicro()),
            nullSafe(sessionData.getTimeRemainingMicro()),
            escape(sessionData.getServiceName())
        );
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
                log.info(">>> [ ✅ 회사별({}) 백업 파일 작성 완료 ]", entry.getKey());
            } catch (IOException e) {
                log.error("CSV 파일 닫기 실패 (Company ID: {}): {}", entry.getKey(), e.getMessage(), e);
            }
        }
    }
}
