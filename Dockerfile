# ==============================================
# STAGE 1: Compilation Environment
# ==============================================
FROM eclipse-temurin:25-jdk-alpine AS builder
WORKDIR /app

# Copy Maven repository wrapper and descriptor
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw

# Resolve dependencies to cache layers effectively
RUN ./mvnw dependency:go-offline -B

# Copy project source and run jar compilation
COPY src ./src
RUN ./mvnw clean package -DskipTests

# ==============================================
# STAGE 2: Microservice Minimal Runtime
# ==============================================
FROM eclipse-temurin:25-jre-alpine
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app
COPY --from=builder /app/target/employee-service-1.0.0-SNAPSHOT.jar app.jar

# Run the container under safe non-privileged user access model
USER appuser

# Configure and expose application microservices port
EXPOSE 8080
ENV PORT=8080

# Configure JVM flags optimized for virtual-threads & containers
ENTRYPOINT ["java", "-XX:+UseG1GC", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]