# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Install necessary packages
RUN apk add --no-cache \
    mysql-client \
    bash \
    curl \
    tzdata

# Set timezone to Asia/Seoul
ENV TZ=Asia/Seoul
RUN cp /usr/share/zoneinfo/Asia/Seoul /etc/localtime

# Create non-root user
RUN addgroup -g 1000 ycs && \
    adduser -D -u 1000 -G ycs ycs

# Create necessary directories
RUN mkdir -p /app/logs /app/backups /app/uploads && \
    chown -R ycs:ycs /app

# Copy JAR from build stage
COPY --from=build --chown=ycs:ycs /app/target/*.jar app.jar

# Copy scripts
COPY --chown=ycs:ycs scripts/*.sh /app/scripts/
RUN chmod +x /app/scripts/*.sh

# Switch to non-root user
USER ycs

# JVM options for production
ENV JAVA_OPTS="-Xms1024m -Xmx2048m \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=200 \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:HeapDumpPath=/app/logs \
    -Djava.security.egd=file:/dev/./urandom \
    -Dspring.profiles.active=prod"

# Health check
HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
    CMD curl -f http://localhost:8080/health/simple || exit 1

# Expose ports
EXPOSE 8080 8081

# Entry point
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]