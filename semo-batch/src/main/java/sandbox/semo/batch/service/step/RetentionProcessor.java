package sandbox.semo.batch.service.step;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import sandbox.semo.domain.monitoring.entity.SessionData;

@Slf4j
@RequiredArgsConstructor
public class RetentionProcessor implements ItemProcessor<SessionData, SessionData> {

    private final String backupPath;
    private BufferedWriter fileWriter;

    @PostConstruct
    public void init() {
        try {
            File backupFile = new File(backupPath);
            File backupDir = backupFile.getParentFile();
            
            if (!backupDir.exists() && !backupDir.mkdirs()) {
                throw new RuntimeException("백업 디렉토리 생성 실패: " + backupDir);
            }
            
            log.info(">>> [ 📁 백업 파일 생성 시작: {} ]", backupPath);
            fileWriter = new BufferedWriter(new FileWriter(backupPath));  // append 모드 제거
            writeHeader();
            
        } catch (IOException e) {
            log.error("CSV 파일 초기화 실패: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void writeHeader() throws IOException {
        fileWriter.write("DEVICE_ID,SID,COLLECTED_AT,SERIAL,USERNAME,COMMAND,COMMAND_NAME," +
            "STATUS,SCHEMA_NAME,OS_USER,PROCESS,MACHINE,PORT,TERMINAL,PROGRAM,TYPE," +
            "SQL_ID,SQL_EXEC_START,SQL_EXEC_ID,SQL_TEXT,MODULE,ACTION,LOGON_TIME," +
            "LAST_CALL_ET,FAILED_OVER,BLOCKING_SESSION_STATUS,EVENT,WAIT_CLASS," +
            "STATE,WAIT_TIME_MICRO,TIME_REMAINING_MICRO,SERVICE_NAME\n");
        fileWriter.flush();
    }

    @Override
    public SessionData process(SessionData sessionData) throws Exception {
        try {
            String csvLine = formatCsvLine(sessionData);
            fileWriter.write(csvLine);
            fileWriter.flush();  // 매 레코드마다 flush
            return sessionData;
        } catch (IOException e) {
            log.error("CSV 파일 작성 실패: {}", e.getMessage(), e);
            throw e;
        }
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
        // 쉼표나 큰따옴표가 포함된 경우 처리
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    @PreDestroy
    public void cleanup() {
        try {
            if (fileWriter != null) {
                fileWriter.flush();
                fileWriter.close();
                log.info(">>> [ ✅ 백업 파일 작성 완료: {} ]", backupPath);
            }
        } catch (IOException e) {
            log.error("CSV 파일 닫기 실패: {}", e.getMessage(), e);
        }
    }
} 