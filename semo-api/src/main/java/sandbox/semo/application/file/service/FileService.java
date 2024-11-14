package sandbox.semo.application.file.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import sandbox.semo.domain.file.dto.CsvFileInfo;

public interface FileService {

    List<CsvFileInfo> getCsvFileListByCompany(Long companyId, LocalDate date);

    ResponseEntity<Resource> downloadCsvFile(String key, Long companyId);
}
