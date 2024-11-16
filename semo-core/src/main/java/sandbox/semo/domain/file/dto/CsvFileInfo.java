package sandbox.semo.domain.file.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
public class CsvFileInfo {

    private String key;
    private String fileName;
    private Long companyId;
    private LocalDateTime lastModified;
    private long fileSize;

    @Builder
    public CsvFileInfo(String key, String fileName, Long companyId, LocalDateTime lastModified,
        long fileSize) {
        this.key = key;
        this.fileName = fileName;
        this.companyId = companyId;
        this.lastModified = lastModified;
        this.fileSize = fileSize;
    }
}
