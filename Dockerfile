# Bước 1: Build (Sử dụng JDK 21)
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Giới hạn dung lượng RAM tối đa cho JVM của Gradle (tránh sập Render)
ENV GRADLE_OPTS="-Dorg.gradle.jvmargs='-Xmx512m -XX:MaxMetaspaceSize=256m'"

# Sao chép file cấu hình trước để tận dụng Docker Cache
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# Cấp quyền cho gradlew
RUN chmod +x gradlew

# Tải trước các thư viện (Dependencies)
RUN ./gradlew dependencies --no-daemon || true

# Sao chép toàn bộ mã nguồn
COPY . .

# Tiến hành build ứng dụng
RUN ./gradlew bootJar -x test --no-daemon --stacktrace

# Bước 2: Chạy (Bản JRE Alpine siêu nhẹ)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]