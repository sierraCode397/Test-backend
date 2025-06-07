#############################################
# 1) “Builder” stage: optional, only if you
#    want Docker to compile/pack the JAR itself.
#############################################
# (Skip this stage if you prefer to build outside Docker
#  and only copy the JAR in the next stage.)

FROM maven:3.9.1-eclipse-temurin-21 AS builder
WORKDIR /app

# Copy only POM and sources, so that Docker caches dependencies:
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

#############################################
# 2) “Runtime” stage: only keep the JRE + JAR
#############################################
FROM eclipse-temurin:21-jdk-jammy
LABEL authors="IsaacLuis"

# Create a non-root user (optional best practice):
RUN useradd -m springuser

# Create an “app” directory and switch into it:
WORKDIR /app

# Copy the fat JAR from the builder (or from your local target/ folder):
COPY --from=builder /app/target/demo-0.0.1-SNAPSHOT.jar ./app.jar

# If you already built locally (outside Docker), skip the builder stage and just:
# COPY target/demo-0.0.1-SNAPSHOT.jar ./app.jar

# Expose the port your Spring Boot app listens on:
# (You set in .env “PORT=8080”, so that becomes server.port.)
EXPOSE 8080

# Switch to non-root user (optional):
USER springuser

# Entrypoint to run the JAR; the “-Dspring.profiles.active=…” can be added if needed.
ENTRYPOINT ["java","-jar","/app/app.jar"]
