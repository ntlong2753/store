# Bước 1: Build
FROM gradle:8.7-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle clean build -x test

# Bước 2: Chạy
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
