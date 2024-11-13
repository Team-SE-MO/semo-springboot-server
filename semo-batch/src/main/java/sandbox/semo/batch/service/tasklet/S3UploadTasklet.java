package sandbox.semo.batch.service.tasklet;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import java.io.File;
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

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${backup.path}")
    private String backupBasePath;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
        throws Exception {
        File backupDir = new File(backupBasePath);
        if (!backupDir.exists() || !backupDir.isDirectory()) {
            log.warn("백업 디렉토리가 존재하지 않습니다: {}", backupBasePath);
            return RepeatStatus.FINISHED;
        }

        File[] files = backupDir.listFiles((dir, name) -> name.endsWith(".csv"));
        if (files == null || files.length == 0) {
            log.warn("업로드할 CSV 파일이 없습니다.");
            return RepeatStatus.FINISHED;
        }

        for (File file : files) {
            try {
                // company_1_20241113.csv 형식에서 회사 ID 추출
                String fileName = file.getName();
                String companyId = fileName.split("_")[1];
                
                // S3 키 생성 (회사별 폴더 구조)
                String s3Key = String.format("session-data/company-%s/%s", 
                    companyId, 
                    fileName);
                
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
        
        return RepeatStatus.FINISHED;
    }
}