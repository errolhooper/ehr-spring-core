FROM eclipse-temurin:17-jre-alpine

# Set working directory
WORKDIR /app

# Copy the application JAR
COPY target/ehr-spring-core-*.jar app.jar

# Expose the application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
