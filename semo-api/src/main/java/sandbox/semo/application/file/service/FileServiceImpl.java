package sandbox.semo.application.file.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    public ResponseEntity<Resource> downloadCsvFile(String key, Long companyId) {
        try {
            log.info(">>> [ 📥 파일 다운로드 시작 - key: {}, companyId: {} ]", key, companyId);

            // S3에서 파일 가져오기
            S3Object s3Object = amazonS3.getObject(bucket, key);
            InputStreamResource resource = new InputStreamResource(s3Object.getObjectContent());

            // 파일명 추출
            String fileName = key.substring(key.lastIndexOf('/') + 1);

            log.info(">>> [ ✅ 파일 다운로드 준비 완료 - fileName: {} ]", fileName);

            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + fileName + "\"")
                .body(resource);

        } catch (AmazonS3Exception e) {
            log.error(">>> [ ❌ S3 파일 다운로드 실패 - key: {}, error: {} ]",
                key, e.getMessage());
            throw new RuntimeException("파일 다운로드 실패", e);
        }
    }
}
