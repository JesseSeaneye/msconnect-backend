# Use Eclipse Temurin (official OpenJDK replacement)
FROM eclipse-temurin:17-jdk-alpine

# Set working directory
WORKDIR /app

# Copy Maven wrapper and project files
COPY .mvn .mvn
COPY mvnw mvnw
COPY pom.xml .

# Make Maven wrapper executable
RUN chmod +x mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline

# Copy source code
COPY src src

# Build the app
RUN ./mvnw package -DskipTests

# Expose port
EXPOSE 8080

# ✅ Run the app with the exact JAR name
CMD ["java", "-jar", "target/maintenance-backend-0.0.1-SNAPSHOT.jar"]