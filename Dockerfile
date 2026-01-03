# Use Java 21 as the base image
FROM eclipse-temurin:21-jre-jammy

# Create a directory for the app
WORKDIR /app

# Copy the JAR from the build folder
# Note: In the GitHub Action, we will SCP the JAR to a folder, 
# and then build the image on the VM using this Dockerfile.
COPY oauth-*.jar app.jar

# Expose the port your app runs on
EXPOSE 9000

# Start the application
ENTRYPOINT ["java", "-jar", "app.jar", "--server.port=9000"]
