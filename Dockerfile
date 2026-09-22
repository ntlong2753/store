# Bước 1: Build (Chuyển về JDK 17)
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# Giới hạn RAM cho Gradle để không bị sập Render
ENV GRADLE_OPTS="-Dorg.gradle.jvmargs='-Xmx512m -XX:MaxMetaspaceSize=256m'"

# Copy file cấu hình trước để tận dụng Docker Cache
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# Cấp quyền thực thi cho gradlew
RUN chmod +x gradlew

# Tải trước dependencies
RUN ./gradlew dependencies --no-daemon || true

# Copy toàn bộ source code
COPY . .

# Chỉ build file executable jar và in stacktrace nếu có lỗi
RUN ./gradlew bootJar -x test --no-daemon --stacktrace

# Bước 2: Chạy (Dùng JRE 17 Alpine)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]