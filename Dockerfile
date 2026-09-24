# ---------------------------------------------------------------------------
# Build stage: compile the application with the Maven wrapper
# ---------------------------------------------------------------------------
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy the Maven wrapper and project descriptor first to leverage Docker
# layer caching between builds.
COPY mvnw ./
COPY .mvn/ .mvn/
COPY pom.xml ./
COPY src ./src

# Package the fat JAR. Tests are intentionally skipped: they are executed by
# the CI workflow (`mvn verify`) and require more resources in the build image.
RUN ./mvnw -B -DskipTests package

# ---------------------------------------------------------------------------
# Runtime stage: minimal JRE image, run as a non-root user
# ---------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Non-root user principle of least privilege
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]