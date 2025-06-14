########################################
# 1) Builder stage: compile & package #
########################################
FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /app

# 1a) Copy only pom.xml and download dependencies into cache
COPY pom.xml ./
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -B

# 1b) Copy sources and build the fat JAR
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -DskipTests -B

#################################################
# 2) Runtime stage: smaller image with the JAR  #
#################################################
FROM eclipse-temurin:21-jdk-jammy
LABEL authors="IsaacLuis"

# Create a non-root user
RUN useradd -m springuser

WORKDIR /app

# Copy only the packaged JAR
COPY --from=builder /app/target/demo-0.0.1-SNAPSHOT.jar app.jar

# Expose port and add a healthcheck
EXPOSE 8080

HEALTHCHECK --interval=10s --timeout=3s --retries=5 \
  CMD curl -f \
       -H "Authorization: Bearer $JWT_SECRET" \
       http://localhost:8080/actuator/health \
    || exit 1

# Drop to non-root
USER springuser

# Entrypoint
ENTRYPOINT ["java","-jar","/app/app.jar"]

