## ===== Stage 1: Build Stage =====
#FROM maven:3.9-eclipse-temurin-21 AS build
#WORKDIR /app
#COPY . .
#RUN mvn -B -DskipTests clean package -s .mvn/settings-test.xml
# # ===== Stage 2: Runtime Stage =====
#FROM eclipse-temurin:21-jre
#WORKDIR /app
#COPY --from=build /app/target/*.jar app.jar
#EXPOSE 8080
#ENTRYPOINT ["java","-jar","app.jar"]


# Stage 1: Builder - Extract Spring Boot fat JAR
FROM eclipse-temurin:21-jre-alpine as builder
WORKDIR /app
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} application.jar
# Extract các layer của Spring Boot
RUN java -Djarmode=layertools -jar application.jar extract

# Stage 2: Production - Chạy app với các layer đã extract
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Tạo user non-root để tăng tính bảo mật
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=builder /app/dependencies/ ./
COPY --from=builder /app/spring-boot-loader/ ./
COPY --from=builder /app/snapshot-dependencies/ ./
COPY --from=builder /app/application/ ./

EXPOSE 8080
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]