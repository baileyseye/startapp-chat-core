# Stage 1: Build
FROM gradle:8.14-jdk21 AS builder
WORKDIR /app
COPY --chown=gradle:gradle . .
RUN gradle clean bootJar --no-daemon

# Stage 2: Run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
