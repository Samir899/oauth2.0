# Stage 1: Build
FROM gradle:8.5-jdk21-alpine AS build
WORKDIR /app
COPY . .
RUN ./gradlew build -x test

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Copy only the built jar from the build stage
# Using a wildcard to match the generated jar name
COPY --from=build /app/build/libs/*.jar app.jar

# Standard Spring Boot port
EXPOSE 9000

# Entrypoint to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]


