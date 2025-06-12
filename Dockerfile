# builder stage
FROM gradle:8.14-jdk21 AS builder
LABEL author="Melvstein"

WORKDIR /app

COPY build.gradle settings.gradle ./
COPY gradle ./gradle
RUN gradle --version

COPY . .

RUN gradle clean bootJar --no-daemon

# runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app

# Enable log color and flush logs properly
ENV JAVA_TOOL_OPTIONS="-Dspring.output.ansi.enabled=ALWAYS -Dfile.encoding=UTF-8"

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
