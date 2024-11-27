# 1. Base image
FROM bellsoft/liberica-openjdk-alpine:17

# 2. Working directory 설정
WORKDIR /app

# 3. JAR 파일 복사
COPY build/libs/semo-batch-collect-0.0.1-SNAPSHOT.jar app.jar

# 4. Port expose (배치 서버 포트 9090)
EXPOSE 9090

# 5. 실행 명령어
CMD ["java", "-jar", "app.jar"]
