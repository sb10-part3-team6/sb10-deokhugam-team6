# Stage 1: Build stage
FROM amazoncorretto:17 AS builder
WORKDIR /app

# Gradle 래퍼 및 설정 파일 복사 (캐싱 활용)
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# 실행 권한 부여 및 의존성 다운로드 (소스 변경 없이 캐시됨)
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon

# 소스 코드 복사 및 실행 가능한 JAR 빌드 (테스트 제외)
COPY src src
RUN ./gradlew bootJar -x test --no-daemon

# 실행 가능한 JAR 파일을 찾아 이름을 app.jar로 고정 (plain JAR 제외)
RUN cp build/libs/*[!plain].jar build/libs/app.jar || cp build/libs/*.jar build/libs/app.jar

# Stage 2: Runtime stage
FROM amazoncorretto:17-alpine
WORKDIR /app

# 빌드 스테이지에서 준비된 app.jar만 복사
COPY --from=builder /app/build/libs/app.jar app.jar

# 스프링 부트 기본 포트 노출
EXPOSE 8080

# 기본 JVM 옵션 설정 (필요 시 런타임에 변경 가능)
ENV JVM_OPTS=""
# 애플리케이션 실행
ENTRYPOINT ["sh", "-c", "java $JVM_OPTS -jar app.jar"]
