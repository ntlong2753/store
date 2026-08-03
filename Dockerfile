# Bước 1: Build (Dùng bộ JDK nguyên gốc thay vì bộ Gradle nặng nề)
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .

# Cấp quyền thực thi cho thanh công cụ gradlew
RUN chmod +x gradlew

# Chạy build siêu tiết kiệm RAM (tắt Daemon để không sập máy chủ Render)
RUN ./gradlew clean build -x test --no-daemon

# Bước 2: Chạy (Vẫn lấy bản JRE Alpine siêu nhẹ)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
