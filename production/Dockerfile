#builder
FROM gradle:8.14-jdk21-alpine AS builder

LABEL author="Melvstein"

WORKDIR /app

COPY build.gradle settings.gradle ./
COPY gradle ./gradle

RUN gradle --version && gradle dependencies
RUN gradle build --no-daemon -x test || true

COPY . .

RUN gradle clean bootJar --no-daemon

#runtime
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]