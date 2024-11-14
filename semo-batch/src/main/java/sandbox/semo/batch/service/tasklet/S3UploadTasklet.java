package sandbox.semo.batch.service.tasklet;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import jakarta.annotation.PostConstruct;
import java.io.File;
import java.util.concurrent.Executors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;

@RequiredArgsConstructor
@Log4j2
public class S3UploadTasklet implements Tasklet {

    private final AmazonS3 amazonS3;
    private TransferManager transferManager;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${backup.path}")
    private String backupBasePath;

    @PostConstruct
    public void init() {
        transferManager = TransferManagerBuilder.standard()
            .withS3Client(amazonS3)
            .withMultipartUploadThreshold((long) (5 * 1024 * 1024))  // 5MB 이상이면 멀티파트
            .withMinimumUploadPartSize((long) (5 * 1024 * 1024))     // 각 파트 크기
            .withExecutorFactory(() -> Executors.newFixedThreadPool(10))
            .build();
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
        throws Exception {

        long startTime = System.currentTimeMillis();

        File backupDir = new File(backupBasePath + "/company");
        if (!backupDir.exists() || !backupDir.isDirectory()) {
            log.warn("백업 디렉토리가 존재하지 않습니다: {}", backupBasePath);
            return RepeatStatus.FINISHED;
        }

        long findStart = System.currentTimeMillis();
        File[] companyDirs = backupDir.listFiles(File::isDirectory);
        long findEnd = System.currentTimeMillis();
        log.info(">>> [ 🔍 디렉토리 검색 소요시간: {}ms ]", findEnd - findStart);

        if (companyDirs == null) {
            log.warn("회사 디렉토리가 없습니다.");
            return RepeatStatus.FINISHED;
        }

        long uploadStart = System.currentTimeMillis();
        for (File companyDir : companyDirs) {
            File[] csvFiles = companyDir.listFiles((dir, name) -> name.endsWith(".csv"));
            if (csvFiles == null) {
                continue;
            }

            for (File file : csvFiles) {
                try {
                    String s3Key = getS3Key(file);
                    PutObjectRequest request = new PutObjectRequest(bucket, s3Key, file);
                    amazonS3.putObject(request);

                    log.info(">>> [ 📤 S3 업로드 완료 - {}/{} ]", bucket, s3Key);

//                if (file.delete()) {
//                    log.info(">>> [ 🗑 로컬 파일 삭제 완료 - {} ]", file.getAbsolutePath());
//                }

                    // Step 실행 정보에 업로드 결과 추가
                    contribution.incrementWriteCount(1);

                } catch (Exception e) {
                    log.error("S3 업로드 실패: {}", file.getName(), e);
                    throw new RuntimeException("S3 업로드 실패", e);
                }
            }

        }
        long uploadEnd = System.currentTimeMillis();

        log.info(">>> [ 📊 처리 시간 분석 ]");
        log.info(">>> 디렉토리 검색: {}ms", findEnd - findStart);
        log.info(">>> 파일 업로드: {}ms", uploadEnd - uploadStart);
        log.info(">>> 전체 처리시간: {}ms", uploadEnd - startTime);

        return RepeatStatus.FINISHED;
    }

    private String getS3Key(File file) {
        String companyId = file.getParentFile().getName();
        String fileName = file.getName();
        String[] dateParts = fileName.split("-");
        String year = dateParts[0];
        String month = dateParts[1];
        String day = dateParts[2];
        // S3 키 생성 (회사별 폴더 구조)
        String s3Key = String.format("session-data/company/%s/%s/%s/%s",
            companyId,
            year,
            month,
            day);
        return s3Key;
    }
}