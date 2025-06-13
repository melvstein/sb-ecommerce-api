# Use Gradle image with JDK 21
FROM gradle:8.14-jdk21-alpine

LABEL author="Melvstein"

# Set working directory
WORKDIR /app

# Copy Gradle wrapper & config files first (for caching)
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle

# Give execution permission to gradlew
RUN chmod +x gradlew

# Copy the full project (can also use volume in dev)
COPY . .

# Expose Spring Boot default port
EXPOSE 8080

# Run Spring Boot app in development mode
CMD ["./gradlew", "bootRun"]
