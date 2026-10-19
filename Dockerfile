# ==============================================================================
# Stage 1: Build Application
# ==============================================================================
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

# Copy maven wrapper and pom.xml first to leverage Docker layer caching
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x ./mvnw

# Pre-fetch dependencies
RUN ./mvnw dependency:go-offline -B

# Copy project source and package JAR (skipping tests since they run in CI)
COPY src/ ./src/
RUN ./mvnw clean package -DskipTests -B

# ==============================================================================
# Stage 2: Minimal Production Runtime
# ==============================================================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Run as non-root user for security compliance
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy built artifact from builder stage
COPY --from=builder /build/target/*.jar app.jar

# Configuration defaults
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
