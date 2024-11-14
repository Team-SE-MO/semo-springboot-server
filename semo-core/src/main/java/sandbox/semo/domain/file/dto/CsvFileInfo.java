package sandbox.semo.domain.file.dto;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;

@Data
public class CsvFileInfo {
    private String key;
    private String fileName;
    private Long companyId;
    private LocalDate fileDate;
    private long fileSize;

    @Builder
    public CsvFileInfo(String key, String fileName, Long companyId, LocalDate fileDate,
        long fileSize) {
        this.key = key;
        this.fileName = fileName;
        this.companyId = companyId;
        this.fileDate = fileDate;
        this.fileSize = fileSize;
    }
}
