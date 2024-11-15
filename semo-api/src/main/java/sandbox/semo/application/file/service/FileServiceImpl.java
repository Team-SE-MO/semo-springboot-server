package sandbox.semo.application.file.service;

import static sandbox.semo.application.file.exception.FileErrorCode.DEVICE_ID_COLUMN_NOT_FOUND;
import static sandbox.semo.application.file.exception.FileErrorCode.FILE_NOT_FOUND;

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
import org.springframework.transaction.annotation.Transactional;
import sandbox.semo.application.file.exception.FileBusinessException;
import sandbox.semo.application.file.exception.FileErrorCode;
import sandbox.semo.domain.file.dto.CsvFileInfo;


@Service
@Log4j2
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Override
    public List<CsvFileInfo> getCsvFileListByCompany(Long companyId, LocalDate date) {
        String prefix = "session-data/company/" + companyId + "/";

        if (date != null) {
            prefix += String.format("%d/%d/%d",
                date.getYear(),
                date.getMonthValue(),
                date.getDayOfMonth());
        }

        log.info(">>> [ 🔍 S3 검색 시작 - prefix: {} ]", prefix);

        List<CsvFileInfo> files = new ArrayList<>();
        ListObjectsV2Request request = new ListObjectsV2Request()
            .withBucketName(bucket)
            .withPrefix(prefix);

        ListObjectsV2Result listing;
        do {
            listing = amazonS3.listObjectsV2(request);

            for (S3ObjectSummary summary : listing.getObjectSummaries()) {
                if (summary.getKey().endsWith(".csv")) {
                    files.add(createS3CsvFileInfo(summary));
                }
            }

            request.setContinuationToken(listing.getNextContinuationToken());
        } while (listing.isTruncated());

        files.sort((a, b) -> b.getFileDate().compareTo(a.getFileDate()));

        log.info(">>> [ 📊 검색 결과 - 총 {}개 파일 ]", files.size());

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

            // 날짜 정보는 회사 ID 다음 세 부분
            LocalDate fileDate = LocalDate.of(
                Integer.parseInt(parts[3]),  // year
                Integer.parseInt(parts[4]),  // month
                Integer.parseInt(parts[5])   // day
            );

            return CsvFileInfo.builder()
                .key(key)                    // S3 객체 키 (전체 경로)
                .fileName(fileName)          // 파일명만
                .companyId(companyId)        // 회사 ID
                .fileDate(fileDate)          // 파일 날짜
                .fileSize(summary.getSize()) // 파일 크기 (bytes)
                .build();

        } catch (Exception e) {
            log.warn("S3 파일 정보 파싱 실패: {}", summary.getKey(), e);
            // 에러가 발생해도 기본 정보는 반환
            return CsvFileInfo.builder()
                .key(summary.getKey())
                .fileName(summary.getKey().substring(summary.getKey().lastIndexOf('/') + 1))
                .fileSize(summary.getSize())
                .build();
        }
    }

    @Override
    public ResponseEntity<Resource> downloadCsvFile(String key, Long companyId, Long deviceId) {
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

            // deviceId가 없으면 전체 파일 다운로드
            if (deviceId == null) {
                return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fileName + "\"")
                    .body(new InputStreamResource(s3Object.getObjectContent()));
            }

            // deviceId가 있는 경우 필터링된 데이터로 새 CSV 생성
            ByteArrayResource filteredResource = filterCsvByDeviceId(
                s3Object.getObjectContent(),
                deviceId
            );

            log.info(">>> [ ✅ 파일 다운로드 준비 완료 - fileName: {} ]", fileName);

            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + fileName + "\"")
                .body(filteredResource);

        } catch (AmazonS3Exception e) {
            log.error(">>> [ ❌ S3 파일 다운로드 실패 - key: {}, error: {} ]",
                key, e.getMessage());
            throw new RuntimeException("파일 다운로드 실패", e);
        } catch (IOException e) {
            log.error(">>> [ ❌ CSV 파일 처리 실패 - error: {} ]", e.getMessage());
            throw new RuntimeException("CSV 파일 처리 실패", e);
        }
    }

    private ByteArrayResource filterCsvByDeviceId(InputStream inputStream, Long deviceId)
        throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            CSVParser parser = new CSVParser(reader,
                CSVFormat.DEFAULT.withFirstRecordAsHeader());
            CSVPrinter printer = new CSVPrinter(
                new OutputStreamWriter(outputStream),
                CSVFormat.DEFAULT.withHeader(
                    parser.getHeaderMap().keySet().toArray(new String[0]))
            );

            // deviceId 컬럼 인덱스 찾기
            int deviceIdIndex = parser.getHeaderMap().get("DEVICE_ID");
            if (deviceIdIndex == -1) {
                log.error(">>> [ ❌ DEVICE_ID 컬럼을 찾을 수 없습니다 ]");
                throw new FileBusinessException(DEVICE_ID_COLUMN_NOT_FOUND);
            }

            int filteredCount = 0;
            // 필터링 및 새 CSV 작성
            for (CSVRecord record : parser) {
                String deviceIdStr = record.get(deviceIdIndex).trim();
                // 숫자로만 이루어진 경우에만 처리
                if (deviceIdStr.matches("\\d+")) {
                    Long recordDeviceId = Long.parseLong(deviceIdStr);
                    if (recordDeviceId.equals(deviceId)) {
                        printer.printRecord(record);
                        filteredCount++;
                    }
                }
            }

            printer.flush();
            printer.close();

            if (filteredCount == 0) {
                log.info(">>> [ ℹ️ DEVICE_ID {}에 해당하는 데이터가 없습니다 ]", deviceId);
                throw new FileBusinessException(FileErrorCode.NO_MATCHING_DEVICE_DATA);
            } else {
                log.info(">>> [ ✅ DEVICE_ID {} 데이터 {} 건 필터링 완료 ]", deviceId, filteredCount);
            }

            return new ByteArrayResource(outputStream.toByteArray());
        }
    }

}