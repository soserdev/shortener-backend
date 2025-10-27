# -------- Build Stage --------
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app
COPY pom.xml .
COPY src ./src

# Build with Maven (skip tests for speed)
RUN mvn clean package -DskipTests

# -------- Runtime Stage --------
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the jar only (assuming target/*.jar is your built app)
COPY --from=build /app/target/*.jar app.jar

# Optional: Add non-root user (for better security)
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Expose app port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
