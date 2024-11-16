package sandbox.semo.application.file.service;

import static sandbox.semo.application.file.exception.FileErrorCode.CSV_PROCESSING_ERROR;
import static sandbox.semo.application.file.exception.FileErrorCode.DEVICE_ID_COLUMN_NOT_FOUND;
import static sandbox.semo.application.file.exception.FileErrorCode.FILE_DOWNLOAD_FAILURE;
import static sandbox.semo.application.file.exception.FileErrorCode.FILE_NOT_FOUND;
import static sandbox.semo.application.file.exception.FileErrorCode.NO_MATCHING_DEVICE_DATA;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import sandbox.semo.application.file.exception.FileBusinessException;
import sandbox.semo.domain.device.repository.DeviceRepository;
import sandbox.semo.domain.file.dto.CsvFileInfo;


@Service
@Log4j2
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final AmazonS3 amazonS3;
    private final DeviceRepository deviceRepository;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private static final String S3_PREFIX = "session-data/company/";

    @Override
    public List<CsvFileInfo> getCsvFileListByCompany(Long companyId, LocalDate date) {
        String prefix = buildPrefix(companyId, date);
        log.info(">>> [ 🔍 S3 검색 시작 - prefix: {} ]", prefix);

        List<CsvFileInfo> files = fetchCsvFilesFromS3(prefix);
        files.sort((a, b) -> b.getLastModified().compareTo(a.getLastModified()));

        log.info(">>> [ 📊 검색 결과 - 총 {}개 파일 ]", files.size());
        return files;
    }

    private String buildPrefix(Long companyId, LocalDate date) {
        String prefix = S3_PREFIX + companyId + "/";
        if (date != null) {
            prefix += String.format("%d/%d/%d",
                date.getYear(),
                date.getMonthValue(),
                date.getDayOfMonth());
        }
        return prefix;
    }

    private List<CsvFileInfo> fetchCsvFilesFromS3(String prefix) {
        List<CsvFileInfo> files = new ArrayList<>();
        ListObjectsV2Request request = new ListObjectsV2Request()
            .withBucketName(bucket)
            .withPrefix(prefix);

        ListObjectsV2Result listing;
        do {
            listing = amazonS3.listObjectsV2(request);
            listing.getObjectSummaries().stream()
                .filter(summary -> summary.getKey().endsWith(".csv"))
                .map(this::createS3CsvFileInfo)
                .forEach(files::add);

            request.setContinuationToken(listing.getNextContinuationToken());
        } while (listing.isTruncated());

        return files;
    }

    private CsvFileInfo createS3CsvFileInfo(S3ObjectSummary summary) {
        try {
            // S3 키에서 정보 추출 (예: session-data/company/1/2024/3/14/file.csv)
            String key = summary.getKey();
            String[] parts = key.split("/");

            // 파일명은 마지막 부분
            String fileName = parts[parts.length - 1];

            // 회사 ID는 세 번째 부분 (company 다음)
            Long companyId = Long.parseLong(parts[2]);

            // S3 객체의 마지막 수정일자를 LocalDateTime으로 변환
            LocalDateTime lastModified = LocalDateTime.ofInstant(
                summary.getLastModified().toInstant(),
                ZoneId.systemDefault()
            );

            return CsvFileInfo.builder()
                .key(key)
                .fileName(fileName)
                .companyId(companyId)
                .lastModified(lastModified)
                .fileSize(summary.getSize())
                .build();

        } catch (Exception e) {
            log.warn("S3 파일 정보 파싱 실패: {}", summary.getKey(), e);
            // 에러가 발생해도 기본 정보는 반환
            return CsvFileInfo.builder()
                .key(summary.getKey())
                .fileName(summary.getKey().substring(summary.getKey().lastIndexOf('/') + 1))
                .lastModified(LocalDateTime.ofInstant(
                    summary.getLastModified().toInstant(),
                    ZoneId.systemDefault()))
                .fileSize(summary.getSize())
                .build();
        }
    }

    @Override
    public ResponseEntity<Resource> downloadCsvFile(String key, Long companyId,
        String deviceAlias) {
        Long deviceId = deviceRepository.findIdByAliasAndCompanyId(deviceAlias, companyId);
        try {
            log.info(">>> [ 📥 파일 다운로드 시작 - key: {}, companyId: {}, deviceId: {} ]",
                key, companyId, deviceId);

            // S3에서 파일 가져오기
            S3Object s3Object = amazonS3.getObject(bucket, key);

            if (!amazonS3.doesObjectExist(bucket, key)) {
                log.error(">>> [ ❌ 파일이 존재하지 않습니다 - key: {} ]", key);
                throw new FileBusinessException(FILE_NOT_FOUND);
            }

            // 원본 파일명 추출
            String originalFileName = key.substring(key.lastIndexOf('/') + 1);
            String fileName = deviceId != null
                ? originalFileName.replace(".csv", "_" + deviceId + ".csv")
                : originalFileName;

            Resource resourceToReturn = createResource(s3Object, deviceId);
            log.info(">>> [ ✅ 파일 다운로드 준비 완료 - fileName: {} ]", fileName);
            return createCsvResponse(fileName, resourceToReturn);
        } catch (AmazonS3Exception e) {
            log.error(">>> [ ❌ S3 파일 다운로드 실패 - key: {}, error: {} ]",
                key, e.getMessage());
            throw new FileBusinessException(FILE_DOWNLOAD_FAILURE);
        } catch (IOException e) {
            log.error(">>> [ ❌ CSV 파일 처리 실패 - error: {} ]", e.getMessage());
            throw new FileBusinessException(CSV_PROCESSING_ERROR);
        }
    }

    private ByteArrayResource filterCsvByDeviceId(InputStream inputStream, Long deviceId)
        throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader());
            CSVPrinter printer = new CSVPrinter(
                new OutputStreamWriter(outputStream),
                CSVFormat.DEFAULT.withHeader(parser.getHeaderNames().toArray(new String[0]))
            )) {

            List<String> headers = parser.getHeaderNames();
            if (!headers.contains("DEVICE_ID")) {
                log.error(">>> [ ❌ DEVICE_ID 컬럼을 찾을 수 없습니다 ]");
                throw new FileBusinessException(DEVICE_ID_COLUMN_NOT_FOUND);
            }

            int filteredCount = 0;
            for (CSVRecord record : parser) {
                String deviceIdStr = record.get("DEVICE_ID").trim();
                if (deviceIdStr.matches("\\d+")) {
                    Long recordDeviceId = Long.parseLong(deviceIdStr);
                    if (recordDeviceId.equals(deviceId)) {
                        printer.printRecord(record);
                        filteredCount++;
                    }
                }
            }

            if (filteredCount == 0) {
                log.info(">>> [ ℹ️ DEVICE_ID {}에 해당하는 데이터가 없습니다 ]", deviceId);
                throw new FileBusinessException(NO_MATCHING_DEVICE_DATA);
            }
            log.info(">>> [ ✅ DEVICE_ID {} 데이터 {} 건 필터링 완료 ]", deviceId, filteredCount);
            return new ByteArrayResource(outputStream.toByteArray());

        }
    }

    private Resource createResource(S3Object s3Object, Long deviceId) throws IOException {
        return deviceId == null
            ? new InputStreamResource(s3Object.getObjectContent())
            : filterCsvByDeviceId(s3Object.getObjectContent(), deviceId);
    }

    private ResponseEntity<Resource> createCsvResponse(String fileName, Resource resource) {
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("text/csv"))
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + fileName + "\"")
            .body(resource);
    }
}
