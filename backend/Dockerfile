# 1단계: 빌드
# OpenJDK와 Gradle을 포함한 이미지로 빌드
FROM gradle:7.5.1-jdk17 AS build

# 작업 디렉토리 설정
WORKDIR /app

# Gradle 캐시 활용을 위해 build.gradle과 settings.gradle 파일을 먼저 복사
COPY build.gradle settings.gradle ./
RUN gradle build -x test --no-daemon || return 0

# 소스 코드 전체 복사
COPY . .

# 애플리케이션 빌드
RUN gradle clean build -x test --no-daemon

# 2단계: 실행
# 경량 OpenJDK 이미지를 기반으로 실행 단계 설정
FROM openjdk:17-jdk-slim

# 작업 디렉토리 생성
WORKDIR /app

# 빌드 단계에서 생성된 JAR 파일을 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
