FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /app

# Copy only POM and sources, so that Docker caches dependencies:
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

FROM eclipse-temurin:21-jdk-jammy
LABEL authors="IsaacLuis"

# Create a non-root user (optional best practice):
RUN useradd -m springuser

# Create an “app” directory and switch into it:
WORKDIR /app

# Copy the fat JAR from the builder (or from your local target/ folder):
COPY --from=builder /app/target/demo-0.0.1-SNAPSHOT.jar ./app.jar

EXPOSE 8080

# Add Docker-native healthcheck for Spring Boot actuator health endpoint:
HEALTHCHECK --interval=10s --timeout=3s --retries=5 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Switch to non-root user (optional):
USER springuser

# Entrypoint to run the JAR; the “-Dspring.profiles.active=…” can be added if needed.
ENTRYPOINT ["java","-jar","/app/app.jar"]