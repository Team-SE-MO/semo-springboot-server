# 1. Base image
FROM bellsoft/liberica-openjdk-alpine:17 as builder

# 2. Working directory 설정
WORKDIR /app

# 3. JAR 파일 복사
COPY ./semo-batch-retention/build/libs/semo-batch-retention-*.jar batch-retention.jar

# 4. Port expose (배치 서버 포트 9091)
EXPOSE 9091

# 5. 실행 명령어
CMD ["java", "-jar", "batch-retention.jar"]
