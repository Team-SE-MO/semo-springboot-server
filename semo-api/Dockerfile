# 1. Base image
FROM bellsoft/liberica-openjdk-alpine:17 as builder

# 2. Working directory 설정
WORKDIR /app

# 3. JAR 파일 복사
COPY semo-app/build/libs/semo-api-*.jar app.jar

# 4. Port expose (API 서버 포트 8080)
EXPOSE 8080

# 5. 실행 명령어
CMD ["java", "-jar", "app.jar"]
