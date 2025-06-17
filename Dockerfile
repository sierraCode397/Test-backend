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

# Expose port 
EXPOSE 8080

# Drop to non-root
USER springuser

# Entrypoint
ENTRYPOINT ["java","-jar","/app/app.jar"]

